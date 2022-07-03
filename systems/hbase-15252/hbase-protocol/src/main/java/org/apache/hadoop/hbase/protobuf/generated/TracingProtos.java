// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Tracing.proto

package org.apache.hadoop.hbase.protobuf.generated;

public final class TracingProtos {
  private TracingProtos() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface RPCTInfoOrBuilder
      extends com.google.protobuf.MessageOrBuilder {

    // optional int64 trace_id = 1;
    /**
     * <code>optional int64 trace_id = 1;</code>
     */
    boolean hasTraceId();
    /**
     * <code>optional int64 trace_id = 1;</code>
     */
    long getTraceId();

    // optional int64 parent_id = 2;
    /**
     * <code>optional int64 parent_id = 2;</code>
     */
    boolean hasParentId();
    /**
     * <code>optional int64 parent_id = 2;</code>
     */
    long getParentId();
  }
  /**
   * Protobuf type {@code hbase.pb.RPCTInfo}
   *
   * <pre>
   *Used to pass through the information necessary to continue
   *a trace after an RPC is made. All we need is the traceid 
   *(so we know the overarching trace this message is a part of), and
   *the id of the current span when this message was sent, so we know 
   *what span caused the new span we will create when this message is received.
   * </pre>
   */
  public static final class RPCTInfo extends
      com.google.protobuf.GeneratedMessage
      implements RPCTInfoOrBuilder {
    // Use RPCTInfo.newBuilder() to construct.
    private RPCTInfo(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private RPCTInfo(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final RPCTInfo defaultInstance;
    public static RPCTInfo getDefaultInstance() {
      return defaultInstance;
    }

    public RPCTInfo getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private RPCTInfo(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 8: {
              bitField0_ |= 0x00000001;
              traceId_ = input.readInt64();
              break;
            }
            case 16: {
              bitField0_ |= 0x00000002;
              parentId_ = input.readInt64();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.apache.hadoop.hbase.protobuf.generated.TracingProtos.internal_static_hbase_pb_RPCTInfo_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.apache.hadoop.hbase.protobuf.generated.TracingProtos.internal_static_hbase_pb_RPCTInfo_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo.class, org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo.Builder.class);
    }

    public static com.google.protobuf.Parser<RPCTInfo> PARSER =
        new com.google.protobuf.AbstractParser<RPCTInfo>() {
      public RPCTInfo parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new RPCTInfo(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<RPCTInfo> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    // optional int64 trace_id = 1;
    public static final int TRACE_ID_FIELD_NUMBER = 1;
    private long traceId_;
    /**
     * <code>optional int64 trace_id = 1;</code>
     */
    public boolean hasTraceId() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional int64 trace_id = 1;</code>
     */
    public long getTraceId() {
      return traceId_;
    }

    // optional int64 parent_id = 2;
    public static final int PARENT_ID_FIELD_NUMBER = 2;
    private long parentId_;
    /**
     * <code>optional int64 parent_id = 2;</code>
     */
    public boolean hasParentId() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional int64 parent_id = 2;</code>
     */
    public long getParentId() {
      return parentId_;
    }

    private void initFields() {
      traceId_ = 0L;
      parentId_ = 0L;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeInt64(1, traceId_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeInt64(2, parentId_);
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(1, traceId_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(2, parentId_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo)) {
        return super.equals(obj);
      }
      org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo other = (org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo) obj;

      boolean result = true;
      result = result && (hasTraceId() == other.hasTraceId());
      if (hasTraceId()) {
        result = result && (getTraceId()
            == other.getTraceId());
      }
      result = result && (hasParentId() == other.hasParentId());
      if (hasParentId()) {
        result = result && (getParentId()
            == other.getParentId());
      }
      result = result &&
          getUnknownFields().equals(other.getUnknownFields());
      return result;
    }

    private int memoizedHashCode = 0;
    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptorForType().hashCode();
      if (hasTraceId()) {
        hash = (37 * hash) + TRACE_ID_FIELD_NUMBER;
        hash = (53 * hash) + hashLong(getTraceId());
      }
      if (hasParentId()) {
        hash = (37 * hash) + PARENT_ID_FIELD_NUMBER;
        hash = (53 * hash) + hashLong(getParentId());
      }
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code hbase.pb.RPCTInfo}
     *
     * <pre>
     *Used to pass through the information necessary to continue
     *a trace after an RPC is made. All we need is the traceid 
     *(so we know the overarching trace this message is a part of), and
     *the id of the current span when this message was sent, so we know 
     *what span caused the new span we will create when this message is received.
     * </pre>
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfoOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.apache.hadoop.hbase.protobuf.generated.TracingProtos.internal_static_hbase_pb_RPCTInfo_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.apache.hadoop.hbase.protobuf.generated.TracingProtos.internal_static_hbase_pb_RPCTInfo_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo.class, org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo.Builder.class);
      }

      // Construct using org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        traceId_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000001);
        parentId_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.apache.hadoop.hbase.protobuf.generated.TracingProtos.internal_static_hbase_pb_RPCTInfo_descriptor;
      }

      public org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo getDefaultInstanceForType() {
        return org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo.getDefaultInstance();
      }

      public org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo build() {
        org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo buildPartial() {
        org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo result = new org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.traceId_ = traceId_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.parentId_ = parentId_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo) {
          return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo other) {
        if (other == org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo.getDefaultInstance()) return this;
        if (other.hasTraceId()) {
          setTraceId(other.getTraceId());
        }
        if (other.hasParentId()) {
          setParentId(other.getParentId());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (org.apache.hadoop.hbase.protobuf.generated.TracingProtos.RPCTInfo) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      // optional int64 trace_id = 1;
      private long traceId_ ;
      /**
       * <code>optional int64 trace_id = 1;</code>
       */
      public boolean hasTraceId() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>optional int64 trace_id = 1;</code>
       */
      public long getTraceId() {
        return traceId_;
      }
      /**
       * <code>optional int64 trace_id = 1;</code>
       */
      public Builder setTraceId(long value) {
        bitField0_ |= 0x00000001;
        traceId_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional int64 trace_id = 1;</code>
       */
      public Builder clearTraceId() {
        bitField0_ = (bitField0_ & ~0x00000001);
        traceId_ = 0L;
        onChanged();
        return this;
      }

      // optional int64 parent_id = 2;
      private long parentId_ ;
      /**
       * <code>optional int64 parent_id = 2;</code>
       */
      public boolean hasParentId() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional int64 parent_id = 2;</code>
       */
      public long getParentId() {
        return parentId_;
      }
      /**
       * <code>optional int64 parent_id = 2;</code>
       */
      public Builder setParentId(long value) {
        bitField0_ |= 0x00000002;
        parentId_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional int64 parent_id = 2;</code>
       */
      public Builder clearParentId() {
        bitField0_ = (bitField0_ & ~0x00000002);
        parentId_ = 0L;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:hbase.pb.RPCTInfo)
    }

    static {
      defaultInstance = new RPCTInfo(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:hbase.pb.RPCTInfo)
  }

  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_hbase_pb_RPCTInfo_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_hbase_pb_RPCTInfo_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\rTracing.proto\022\010hbase.pb\"/\n\010RPCTInfo\022\020\n" +
      "\010trace_id\030\001 \001(\003\022\021\n\tparent_id\030\002 \001(\003B@\n*or" +
      "g.apache.hadoop.hbase.protobuf.generated" +
      "B\rTracingProtosH\001\240\001\001"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_hbase_pb_RPCTInfo_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_hbase_pb_RPCTInfo_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_hbase_pb_RPCTInfo_descriptor,
              new java.lang.String[] { "TraceId", "ParentId", });
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}
