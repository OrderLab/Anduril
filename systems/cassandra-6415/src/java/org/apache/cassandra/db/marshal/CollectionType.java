/*
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
package org.apache.cassandra.db.marshal;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.serializers.MarshalException;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.Pair;

/**
 * The abstract validator that is the base for maps, sets and lists.
 *
 * Please note that this comparator shouldn't be used "manually" (through thrift for instance).
 *
 */
public abstract class CollectionType<T> extends AbstractType<T>
{
    public enum Kind
    {
        MAP, SET, LIST
    }

    public final Kind kind;

    protected CollectionType(Kind kind)
    {
        this.kind = kind;
    }

    public abstract AbstractType<?> nameComparator();
    public abstract AbstractType<?> valueComparator();

    protected abstract void appendToStringBuilder(StringBuilder sb);

    public abstract ByteBuffer serialize(List<Pair<ByteBuffer, Column>> columns);

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        appendToStringBuilder(sb);
        return sb.toString();
    }

    public int compare(ByteBuffer o1, ByteBuffer o2)
    {
        throw new UnsupportedOperationException("CollectionType should not be use directly as a comparator");
    }

    public String getString(ByteBuffer bytes)
    {
        return BytesType.instance.getString(bytes);
    }

    public ByteBuffer fromString(String source)
    {
        try
        {
            return ByteBufferUtil.hexToBytes(source);
        }
        catch (NumberFormatException e)
        {
            throw new MarshalException(String.format("cannot parse '%s' as hex bytes", source), e);
        }
    }

    public void validate(ByteBuffer bytes)
    {
        valueComparator().validate(bytes);
    }

    public boolean isCollection()
    {
        return true;
    }

    // Utilitary method
    protected static ByteBuffer pack(List<ByteBuffer> buffers, int elements, int size)
    {
        ByteBuffer result = ByteBuffer.allocate(2 + size);
        result.putShort((short)elements);
        for (ByteBuffer bb : buffers)
        {
            result.putShort((short)bb.remaining());
            result.put(bb.duplicate());
        }
        return (ByteBuffer)result.flip();
    }

    public static ByteBuffer pack(List<ByteBuffer> buffers, int elements)
    {
        int size = 0;
        for (ByteBuffer bb : buffers)
            size += 2 + bb.remaining();
        return pack(buffers, elements, size);
    }

    protected static int getUnsignedShort(ByteBuffer bb)
    {
        int length = (bb.get() & 0xFF) << 8;
        return length | (bb.get() & 0xFF);
    }

    public CQL3Type asCQL3Type()
    {
        return new CQL3Type.Collection(this);
    }
}
