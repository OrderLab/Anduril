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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import org.apache.cassandra.db.filter.ColumnSlice;
import org.apache.cassandra.db.filter.SliceQueryFilter;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.SyntaxException;
import org.apache.cassandra.cql3.ColumnNameBuilder;
import org.apache.cassandra.cql3.Relation;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.apache.cassandra.serializers.MarshalException;
import org.apache.cassandra.utils.ByteBufferUtil;

/*
 * The encoding of a CompositeType column name should be:
 *   <component><component><component> ...
 * where <component> is:
 *   <length of value><value><'end-of-component' byte>
 * where <length of value> is a 2 bytes unsigned short the and the
 * 'end-of-component' byte should always be 0 for actual column name.
 * However, it can set to 1 for query bounds. This allows to query for the
 * equivalent of 'give me the full super-column'. That is, if during a slice
 * query uses:
 *   start = <3><"foo".getBytes()><0>
 *   end   = <3><"foo".getBytes()><1>
 * then he will be sure to get *all* the columns whose first component is "foo".
 * If for a component, the 'end-of-component' is != 0, there should not be any
 * following component. The end-of-component can also be -1 to allow
 * non-inclusive query. For instance:
 *   start = <3><"foo".getBytes()><-1>
 * allows to query everything that is greater than <3><"foo".getBytes()>, but
 * not <3><"foo".getBytes()> itself.
 */
public class CompositeType extends AbstractCompositeType
{
    public final List<AbstractType<?>> types;

    // interning instances
    private static final Map<List<AbstractType<?>>, CompositeType> instances = new HashMap<List<AbstractType<?>>, CompositeType>();

    public static CompositeType getInstance(TypeParser parser) throws ConfigurationException, SyntaxException
    {
        return getInstance(parser.getTypeParameters());
    }

    public static CompositeType getInstance(AbstractType... types)
    {
        return getInstance(Arrays.<AbstractType<?>>asList(types));
    }

    public static synchronized CompositeType getInstance(List<AbstractType<?>> types)
    {
        assert types != null && !types.isEmpty();

        CompositeType ct = instances.get(types);
        if (ct == null)
        {
            ct = new CompositeType(types);
            instances.put(types, ct);
        }
        return ct;
    }

    private CompositeType(List<AbstractType<?>> types)
    {
        this.types = ImmutableList.copyOf(types);
    }

    protected AbstractType<?> getComparator(int i, ByteBuffer bb)
    {
        return types.get(i);
    }

    protected AbstractType<?> getComparator(int i, ByteBuffer bb1, ByteBuffer bb2)
    {
        return types.get(i);
    }

    protected AbstractType<?> getAndAppendComparator(int i, ByteBuffer bb, StringBuilder sb)
    {
        return types.get(i);
    }

    protected ParsedComparator parseComparator(int i, String part)
    {
        return new StaticParsedComparator(types.get(i), part);
    }

    protected AbstractType<?> validateComparator(int i, ByteBuffer bb) throws MarshalException
    {
        if (i >= types.size())
            throw new MarshalException("Too many bytes for comparator");
        return types.get(i);
    }

    public ByteBuffer decompose(Object... objects)
    {
        assert objects.length == types.size();

        ByteBuffer[] serialized = new ByteBuffer[objects.length];
        for (int i = 0; i < objects.length; i++)
        {
            ByteBuffer buffer = ((AbstractType) types.get(i)).decompose(objects[i]);
            serialized[i] = buffer;
        }
        return build(serialized);
    }

    // Extract component idx from bb. Return null if there is not enough component.
    public static ByteBuffer extractComponent(ByteBuffer bb, int idx)
    {
        bb = bb.duplicate();
        int i = 0;
        while (bb.remaining() > 0)
        {
            ByteBuffer c = getWithShortLength(bb);
            if (i == idx)
                return c;

            bb.get(); // skip end-of-component
            ++i;
        }
        return null;
    }

    // Extract CQL3 column name from the full column name.
    public ByteBuffer extractLastComponent(ByteBuffer bb)
    {
        int idx = types.get(types.size() - 1) instanceof ColumnToCollectionType ? types.size() - 2 : types.size() - 1;
        return extractComponent(bb, idx);
    }

    @Override
    public int componentsCount()
    {
        return types.size();
    }

    @Override
    public List<AbstractType<?>> getComponents()
    {
        return types;
    }

    @Override
    public boolean isCompatibleWith(AbstractType<?> previous)
    {
        if (this == previous)
            return true;

        if (!(previous instanceof CompositeType))
            return false;

        // Extending with new components is fine
        CompositeType cp = (CompositeType)previous;
        if (types.size() < cp.types.size())
            return false;

        for (int i = 0; i < cp.types.size(); i++)
        {
            AbstractType tprev = cp.types.get(i);
            AbstractType tnew = types.get(i);
            if (!tnew.isCompatibleWith(tprev))
                return false;
        }
        return true;
    }

    @Override
    public boolean isValueCompatibleWith(AbstractType<?> previous)
    {
        if (this == previous)
            return true;

        if (!(previous instanceof CompositeType))
            return false;

        // Extending with new components is fine
        CompositeType cp = (CompositeType)previous;
        if (types.size() < cp.types.size())
            return false;

        for (int i = 0; i < cp.types.size(); i++)
        {
            AbstractType tprev = cp.types.get(i);
            AbstractType tnew = types.get(i);
            if (!tnew.isValueCompatibleWith(tprev))
                return false;
        }
        return true;
    }

    @Override
    public boolean intersects(List<ByteBuffer> minColumnNames, List<ByteBuffer> maxColumnNames, SliceQueryFilter filter)
    {
        assert minColumnNames.size() == maxColumnNames.size();
        outer:
        for (ColumnSlice slice : filter.slices)
        {
            // This slices intersects if all component intersect. And we don't intersect
            // only if no slice intersects
            ByteBuffer[] start = split(filter.isReversed() ? slice.finish : slice.start);
            ByteBuffer[] finish = split(filter.isReversed() ? slice.start : slice.finish);
            for (int i = 0; i < minColumnNames.size(); i++)
            {
                AbstractType<?> t = types.get(i);
                ByteBuffer s = i < start.length ? start[i] : ByteBufferUtil.EMPTY_BYTE_BUFFER;
                ByteBuffer f = i < finish.length ? finish[i] : ByteBufferUtil.EMPTY_BYTE_BUFFER;
                if (!t.intersects(minColumnNames.get(i), maxColumnNames.get(i), s, f))
                    continue outer;
            }
            return true;
        }
        return false;
    }

    private static class StaticParsedComparator implements ParsedComparator
    {
        final AbstractType<?> type;
        final String part;

        StaticParsedComparator(AbstractType<?> type, String part)
        {
            this.type = type;
            this.part = part;
        }

        public AbstractType<?> getAbstractType()
        {
            return type;
        }

        public String getRemainingPart()
        {
            return part;
        }

        public int getComparatorSerializedSize()
        {
            return 0;
        }

        public void serializeComparator(ByteBuffer bb) {}
    }

    @Override
    public String toString()
    {
        return getClass().getName() + TypeParser.stringifyTypeParameters(types);
    }

    public Builder builder()
    {
        return new Builder(this);
    }

    public static ByteBuffer build(ByteBuffer... buffers)
    {
        int totalLength = 0;
        for (ByteBuffer bb : buffers)
            totalLength += 2 + bb.remaining() + 1;

        ByteBuffer out = ByteBuffer.allocate(totalLength);
        for (ByteBuffer bb : buffers)
        {
            putShortLength(out, bb.remaining());
            out.put(bb.duplicate());
            out.put((byte) 0);
        }
        out.flip();
        return out;
    }

    public static class Builder implements ColumnNameBuilder
    {
        private final CompositeType composite;

        private final List<ByteBuffer> components;
        private final byte[] endOfComponents;
        private int serializedSize;

        public Builder(CompositeType composite)
        {
            this(composite, new ArrayList<ByteBuffer>(composite.types.size()), new byte[composite.types.size()]);
        }

        public Builder(CompositeType composite, List<ByteBuffer> components, byte[] endOfComponents)
        {
            assert endOfComponents.length == composite.types.size();

            this.composite = composite;
            this.components = components;
            this.endOfComponents = endOfComponents;
        }

        private Builder(Builder b)
        {
            this(b.composite, new ArrayList<ByteBuffer>(b.components), Arrays.copyOf(b.endOfComponents, b.endOfComponents.length));
            this.serializedSize = b.serializedSize;
        }

        public Builder add(ByteBuffer buffer, Relation.Type op)
        {
            if (components.size() >= composite.types.size())
                throw new IllegalStateException("Composite column is already fully constructed");

            int current = components.size();
            components.add(buffer);

            /*
             * Given the rules for eoc (end-of-component, see AbstractCompositeType.compare()),
             * We can select:
             *   - = 'a' by using <'a'><0>
             *   - < 'a' by using <'a'><-1>
             *   - <= 'a' by using <'a'><1>
             *   - > 'a' by using <'a'><1>
             *   - >= 'a' by using <'a'><0>
             */
            switch (op)
            {
                case LT:
                    endOfComponents[current] = (byte) -1;
                    break;
                case GT:
                case LTE:
                    endOfComponents[current] = (byte) 1;
                    break;
                default:
                    endOfComponents[current] = (byte) 0;
                    break;
            }
            return this;
        }

        public Builder add(ByteBuffer bb)
        {
            return add(bb, Relation.Type.EQ);
        }

        public int componentCount()
        {
            return components.size();
        }

        public int remainingCount()
        {
            return composite.types.size() - components.size();
        }

        public ByteBuffer get(int i)
        {
            return components.get(i);
        }

        public ByteBuffer build()
        {
            DataOutputBuffer out = new DataOutputBuffer(serializedSize);
            for (int i = 0; i < components.size(); i++)
            {
                try
                {
                    ByteBufferUtil.writeWithShortLength(components.get(i), out);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
                out.write(endOfComponents[i]);
            }
            return ByteBuffer.wrap(out.getData(), 0, out.getLength());
        }

        public ByteBuffer buildAsEndOfRange()
        {
            if (components.isEmpty())
                return ByteBufferUtil.EMPTY_BYTE_BUFFER;

            ByteBuffer bb = build();
            bb.put(bb.remaining() - 1, (byte)1);
            return bb;
        }

        public Builder copy()
        {
            return new Builder(this);
        }

        public ByteBuffer getComponent(int i)
        {
            if (i >= components.size())
                throw new IllegalArgumentException();

            return components.get(i);
        }
    }
}
