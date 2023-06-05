/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cassandra.db;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.fail;

import org.apache.cassandra.db.context.CounterContext;
import org.apache.cassandra.SchemaLoader;
import org.apache.cassandra.utils.*;
import org.apache.cassandra.Util;
import static org.apache.cassandra.db.context.CounterContext.ContextState;

public class CounterMutationTest extends SchemaLoader
{
    @Test
    public void testMergeOldShards() throws IOException
    {
        RowMutation rm;
        CounterMutation cm;

        CounterId id1 = CounterId.getLocalId();

        rm = new RowMutation("Keyspace1", ByteBufferUtil.bytes("key1"));
        rm.addCounter("Counter1", ByteBufferUtil.bytes("Column1"), 3);
        cm = new CounterMutation(rm, ConsistencyLevel.ONE);
        cm.apply();

        CounterId.renewLocalId(2L); // faking time of renewal for test
        CounterId id2 = CounterId.getLocalId();

        rm = new RowMutation("Keyspace1", ByteBufferUtil.bytes("key1"));
        rm.addCounter("Counter1", ByteBufferUtil.bytes("Column1"), 4);
        cm = new CounterMutation(rm, ConsistencyLevel.ONE);
        cm.apply();

        CounterId.renewLocalId(4L); // faking time of renewal for test
        CounterId id3 = CounterId.getLocalId();

        rm = new RowMutation("Keyspace1", ByteBufferUtil.bytes("key1"));
        rm.addCounter("Counter1", ByteBufferUtil.bytes("Column1"), 5);
        rm.addCounter("Counter1", ByteBufferUtil.bytes("Column2"), 1);
        cm = new CounterMutation(rm, ConsistencyLevel.ONE);
        cm.apply();

        DecoratedKey dk = Util.dk("key1");
        ColumnFamily cf = Util.getColumnFamily(Keyspace.open("Keyspace1"), dk, "Counter1");

        // First merges old shards
        CounterColumn.mergeAndRemoveOldShards(dk, cf, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
        long now = System.currentTimeMillis();
        Column c = cf.getColumn(ByteBufferUtil.bytes("Column1"));
        assert c != null;
        assert c instanceof CounterColumn;
        assert ((CounterColumn)c).total() == 12L;
        ContextState s = new ContextState(c.value());
        assert s.getCounterId().equals(id1);
        assert s.getCount() == 0L;
        assert -s.getClock() > now - 1000 : " >";
        assert -s.getClock() <= now;
        s.moveToNext();
        assert s.getCounterId().equals(id2);
        assert s.getCount() == 0L;
        assert -s.getClock() > now - 1000;
        assert -s.getClock() <= now;
        s.moveToNext();
        assert s.getCounterId().equals(id3);
        assert s.getCount() == 12L;

        // Then collect old shards
        CounterColumn.mergeAndRemoveOldShards(dk, cf, Integer.MAX_VALUE, Integer.MIN_VALUE, false);
        c = cf.getColumn(ByteBufferUtil.bytes("Column1"));
        assert c != null;
        assert c instanceof CounterColumn;
        assert ((CounterColumn)c).total() == 12L;
        s = new ContextState(c.value());
        assert s.getCounterId().equals(id3);
        assert s.getCount() == 12L;
    }

    @Test
    public void testGetOldShardFromSystemKeyspace() throws IOException
    {
        // Renewing a bunch of times and checking we get the same thing from
        // the system keyspace that what is in memory
        CounterId.renewLocalId();
        CounterId.renewLocalId();
        CounterId.renewLocalId();

        List<CounterId.CounterIdRecord> inMem = CounterId.getOldLocalCounterIds();
        List<CounterId.CounterIdRecord> onDisk = SystemKeyspace.getOldLocalCounterIds();

        assert inMem.equals(onDisk);
    }

    @Test
    public void testRemoveOldShardFixCorrupted() throws IOException
    {
        CounterContext ctx = CounterContext.instance();
        int now = (int) (System.currentTimeMillis() / 1000);

        // Check that corrupted context created prior to #2968 are fixed by removeOldShards
        CounterId id1 = CounterId.getLocalId();
        CounterId.renewLocalId();
        CounterId id2 = CounterId.getLocalId();

        ContextState state = ContextState.allocate(3, 2);
        state.writeElement(CounterId.fromInt(1), 1, 4, false);
        state.writeElement(id1, 3, 2, true);
        state.writeElement(id2, -100, 5, true); // corrupted!

        assert ctx.total(state.context) == 11;

        try
        {
            ByteBuffer merger = ctx.computeOldShardMerger(state.context, Collections.<CounterId.CounterIdRecord>emptyList(), 0);
            ctx.removeOldShards(ctx.merge(state.context, merger, HeapAllocator.instance), now);
            fail("RemoveOldShards should throw an exception if the current id is non-sensical");
        }
        catch (RuntimeException e) {}

        CounterId.renewLocalId();
        ByteBuffer merger = ctx.computeOldShardMerger(state.context, Collections.<CounterId.CounterIdRecord>emptyList(), 0);
        ByteBuffer cleaned = ctx.removeOldShards(ctx.merge(state.context, merger, HeapAllocator.instance), now);
        assert ctx.total(cleaned) == 11;

        // Check it is not corrupted anymore
        ContextState state2 = new ContextState(cleaned);
        while (state2.hasRemaining())
        {
            assert state2.getClock() >= 0 || state2.getCount() == 0;
            state2.moveToNext();
        }

        // Check that if we merge old and clean on another node, we keep the right count
        ByteBuffer onRemote = ctx.merge(ctx.clearAllDelta(state.context), ctx.clearAllDelta(cleaned), HeapAllocator.instance);
        assert ctx.total(onRemote) == 11;
    }
}

