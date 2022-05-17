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

package org.apache.hadoop.ipc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

import javax.net.SocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.UTF8;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.ipc.Client.ConnectionId;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.token.SecretManager;
import org.apache.hadoop.security.token.TokenIdentifier;

/** An RpcEngine implementation for Writable data. */
@InterfaceStability.Evolving
public class WritableRpcEngine implements RpcEngine {
  private static final Log LOG = LogFactory.getLog(RPC.class);
  
  //writableRpcVersion should be updated if there is a change
  //in format of the rpc messages.
  public static long writableRpcVersion = 1L;

  /** A method invocation, including the method name and its parameters.*/
  private static class Invocation implements Writable, Configurable {
    private String methodName;
    private Class<?>[] parameterClasses;
    private Object[] parameters;
    private Configuration conf;
    private long clientVersion;
    private int clientMethodsHash;
    
    //This could be different from static writableRpcVersion when received
    //at server, if client is using a different version.
    private long rpcVersion;

    public Invocation() {}

    public Invocation(Method method, Object[] parameters) {
      this.methodName = method.getName();
      this.parameterClasses = method.getParameterTypes();
      this.parameters = parameters;
      rpcVersion = writableRpcVersion;
      if (method.getDeclaringClass().equals(VersionedProtocol.class)) {
        //VersionedProtocol is exempted from version check.
        clientVersion = 0;
        clientMethodsHash = 0;
      } else {
        try {
          Field versionField = method.getDeclaringClass().getField("versionID");
          versionField.setAccessible(true);
          this.clientVersion = versionField.getLong(method.getDeclaringClass());
        } catch (NoSuchFieldException ex) {
          throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
          throw new RuntimeException(ex);
        }
        this.clientMethodsHash = ProtocolSignature.getFingerprint(method
            .getDeclaringClass().getMethods());
      }
    }

    /** The name of the method invoked. */
    public String getMethodName() { return methodName; }

    /** The parameter classes. */
    public Class<?>[] getParameterClasses() { return parameterClasses; }

    /** The parameter instances. */
    public Object[] getParameters() { return parameters; }
    
    private long getProtocolVersion() {
      return clientVersion;
    }

    private int getClientMethodsHash() {
      return clientMethodsHash;
    }
    
    /**
     * Returns the rpc version used by the client.
     * @return rpcVersion
     */
    public long getRpcVersion() {
      return rpcVersion;
    }

    public void readFields(DataInput in) throws IOException {
      rpcVersion = in.readLong();
      methodName = UTF8.readString(in);
      clientVersion = in.readLong();
      clientMethodsHash = in.readInt();
      parameters = new Object[in.readInt()];
      parameterClasses = new Class[parameters.length];
      ObjectWritable objectWritable = new ObjectWritable();
      for (int i = 0; i < parameters.length; i++) {
        parameters[i] = ObjectWritable.readObject(in, objectWritable, this.conf);
        parameterClasses[i] = objectWritable.getDeclaredClass();
      }
    }

    public void write(DataOutput out) throws IOException {
      out.writeLong(rpcVersion);
      UTF8.writeString(out, methodName);
      out.writeLong(clientVersion);
      out.writeInt(clientMethodsHash);
      out.writeInt(parameterClasses.length);
      for (int i = 0; i < parameterClasses.length; i++) {
        ObjectWritable.writeObject(out, parameters[i], parameterClasses[i],
                                   conf, true);
      }
    }

    public String toString() {
      StringBuilder buffer = new StringBuilder();
      buffer.append(methodName);
      buffer.append("(");
      for (int i = 0; i < parameters.length; i++) {
        if (i != 0)
          buffer.append(", ");
        buffer.append(parameters[i]);
      }
      buffer.append(")");
      buffer.append(", rpc version="+rpcVersion);
      buffer.append(", client version="+clientVersion);
      buffer.append(", methodsFingerPrint="+clientMethodsHash);
      return buffer.toString();
    }

    public void setConf(Configuration conf) {
      this.conf = conf;
    }

    public Configuration getConf() {
      return this.conf;
    }

  }

  private static ClientCache CLIENTS=new ClientCache();
  
  private static class Invoker implements RpcInvocationHandler {
    private Client.ConnectionId remoteId;
    private Client client;
    private boolean isClosed = false;

    public Invoker(Class<?> protocol,
                   InetSocketAddress address, UserGroupInformation ticket,
                   Configuration conf, SocketFactory factory,
                   int rpcTimeout) throws IOException {
      this.remoteId = Client.ConnectionId.getConnectionId(address, protocol,
          ticket, rpcTimeout, conf);
      this.client = CLIENTS.getClient(conf, factory);
    }

    public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {
      long startTime = 0;
      if (LOG.isDebugEnabled()) {
        startTime = System.currentTimeMillis();
      }

      ObjectWritable value = (ObjectWritable)
        client.call(new Invocation(method, args), remoteId);
      if (LOG.isDebugEnabled()) {
        long callTime = System.currentTimeMillis() - startTime;
        LOG.debug("Call: " + method.getName() + " " + callTime);
      }
      return value.get();
    }
    
    /* close the IPC client that's responsible for this invoker's RPCs */ 
    synchronized private void close() {
      if (!isClosed) {
        isClosed = true;
        CLIENTS.stopClient(client);
      }
    }

    @Override
    public ConnectionId getConnectionId() {
      return remoteId;
    }
  }
  
  // for unit testing only
  @InterfaceAudience.Private
  @InterfaceStability.Unstable
  static Client getClient(Configuration conf) {
    return CLIENTS.getClient(conf);
  }
  
  /** Construct a client-side proxy object that implements the named protocol,
   * talking to a server at the named address. 
   * @param <T>*/
  @Override
  @SuppressWarnings("unchecked")
  public <T> ProtocolProxy<T> getProxy(Class<T> protocol, long clientVersion,
                         InetSocketAddress addr, UserGroupInformation ticket,
                         Configuration conf, SocketFactory factory,
                         int rpcTimeout)
    throws IOException {    

    T proxy = (T) Proxy.newProxyInstance(protocol.getClassLoader(),
        new Class[] { protocol }, new Invoker(protocol, addr, ticket, conf,
            factory, rpcTimeout));
    return new ProtocolProxy<T>(protocol, proxy, true);
  }

  /**
   * Stop this proxy and release its invoker's resource
   * @param proxy the proxy to be stopped
   */
  public void stopProxy(Object proxy) {
    ((Invoker)Proxy.getInvocationHandler(proxy)).close();
  }

  
  /** Expert: Make multiple, parallel calls to a set of servers. */
  public Object[] call(Method method, Object[][] params,
                       InetSocketAddress[] addrs, 
                       UserGroupInformation ticket, Configuration conf)
    throws IOException, InterruptedException {

    Invocation[] invocations = new Invocation[params.length];
    for (int i = 0; i < params.length; i++)
      invocations[i] = new Invocation(method, params[i]);
    Client client = CLIENTS.getClient(conf);
    try {
    Writable[] wrappedValues = 
      client.call(invocations, addrs, method.getDeclaringClass(), ticket, conf);
    
    if (method.getReturnType() == Void.TYPE) {
      return null;
    }

    Object[] values =
      (Object[])Array.newInstance(method.getReturnType(), wrappedValues.length);
    for (int i = 0; i < values.length; i++)
      if (wrappedValues[i] != null)
        values[i] = ((ObjectWritable)wrappedValues[i]).get();
    
    return values;
    } finally {
      CLIENTS.stopClient(client);
    }
  }

  /** Construct a server for a protocol implementation instance listening on a
   * port and address. */
  public Server getServer(Class<?> protocol,
                          Object instance, String bindAddress, int port,
                          int numHandlers, int numReaders, int queueSizePerHandler,
                          boolean verbose, Configuration conf,
                      SecretManager<? extends TokenIdentifier> secretManager) 
    throws IOException {
    return new Server(instance, conf, bindAddress, port, numHandlers, 
        numReaders, queueSizePerHandler, verbose, secretManager);
  }
  
  @Override
  public org.apache.hadoop.ipc.RPC.Server getServer(Class<?> protocol,
      Object instance, String bindAddress, int port, int numHandlers,
      int numReaders, int queueSizePerHandler, boolean verbose,
      Configuration conf,
      SecretManager<? extends TokenIdentifier> secretManager,
      String portRangeConfig) throws IOException {
    return new Server(instance, conf, bindAddress, port, numHandlers, 
        numReaders, queueSizePerHandler, verbose, secretManager,
        portRangeConfig);
  }

  /** An RPC Server. */
  public static class Server extends RPC.Server {
    private Object instance;
    private boolean verbose;

    /** Construct an RPC server.
     * @param instance the instance whose methods will be called
     * @param conf the configuration to use
     * @param bindAddress the address to bind on to listen for connection
     * @param port the port to listen for connections on
     */
    public Server(Object instance, Configuration conf, String bindAddress, int port) 
      throws IOException {
      this(instance, conf,  bindAddress, port, 1, -1, -1, false, null);
    }
    
    private static String classNameBase(String className) {
      String[] names = className.split("\\.", -1);
      if (names == null || names.length == 0) {
        return className;
      }
      return names[names.length-1];
    }
    
    /** Construct an RPC server.
     * @param instance the instance whose methods will be called
     * @param conf the configuration to use
     * @param bindAddress the address to bind on to listen for connection
     * @param port the port to listen for connections on
     * @param numHandlers the number of method handler threads to run
     * @param verbose whether each call should be logged
     */
    public Server(Object instance, Configuration conf, String bindAddress,  int port,
        int numHandlers, int numReaders, int queueSizePerHandler, boolean verbose, 
        SecretManager<? extends TokenIdentifier> secretManager) 
    throws IOException {
      this(instance, conf, bindAddress, port, numHandlers, numReaders,
          queueSizePerHandler, verbose, secretManager, null);
    }
    
    public Server(Object instance, Configuration conf, String bindAddress,  int port,
                  int numHandlers, int numReaders, int queueSizePerHandler, boolean verbose, 
                  SecretManager<? extends TokenIdentifier> secretManager, 
                  String portRangeConfig) 
        throws IOException {
      super(bindAddress, port, Invocation.class, numHandlers, numReaders,
          queueSizePerHandler, conf,
          classNameBase(instance.getClass().getName()), secretManager, 
          portRangeConfig);
      this.instance = instance;
      this.verbose = verbose;
    }

    public Writable call(Class<?> protocol, Writable param, long receivedTime) 
    throws IOException {
      try {
        Invocation call = (Invocation)param;
        if (verbose) log("Call: " + call);

        Method method = protocol.getMethod(call.getMethodName(),
                                           call.getParameterClasses());
        method.setAccessible(true);

        // Verify rpc version
        if (call.getRpcVersion() != writableRpcVersion) {
          // Client is using a different version of WritableRpc
          throw new IOException(
              "WritableRpc version mismatch, client side version="
                  + call.getRpcVersion() + ", server side version="
                  + writableRpcVersion);
        }
        
        //Verify protocol version.
        //Bypass the version check for VersionedProtocol
        if (!method.getDeclaringClass().equals(VersionedProtocol.class)) {
          long clientVersion = call.getProtocolVersion();
          ProtocolSignature serverInfo = ((VersionedProtocol) instance)
              .getProtocolSignature(protocol.getCanonicalName(), call
                  .getProtocolVersion(), call.getClientMethodsHash());
          long serverVersion = serverInfo.getVersion();
          if (serverVersion != clientVersion) {
            LOG.warn("Version mismatch: client version=" + clientVersion
                + ", server version=" + serverVersion);
            throw new RPC.VersionMismatch(protocol.getName(), clientVersion,
                serverVersion);
          }
        }

        long startTime = System.currentTimeMillis();
        Object value = method.invoke(instance, call.getParameters());
        int processingTime = (int) (System.currentTimeMillis() - startTime);
        int qTime = (int) (startTime-receivedTime);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Served: " + call.getMethodName() +
                    " queueTime= " + qTime +
                    " procesingTime= " + processingTime);
        }
        rpcMetrics.addRpcQueueTime(qTime);
        rpcMetrics.addRpcProcessingTime(processingTime);
        rpcDetailedMetrics.addProcessingTime(call.getMethodName(),
                                             processingTime);
        if (verbose) log("Return: "+value);

        return new ObjectWritable(method.getReturnType(), value);

      } catch (InvocationTargetException e) {
        Throwable target = e.getTargetException();
        if (target instanceof IOException) {
          throw (IOException)target;
        } else {
          IOException ioe = new IOException(target.toString());
          ioe.setStackTrace(target.getStackTrace());
          throw ioe;
        }
      } catch (Throwable e) {
        if (!(e instanceof IOException)) {
          LOG.error("Unexpected throwable object ", e);
        }
        IOException ioe = new IOException(e.toString());
        ioe.setStackTrace(e.getStackTrace());
        throw ioe;
      }
    }
  }

  private static void log(String value) {
    if (value!= null && value.length() > 55)
      value = value.substring(0, 55)+"...";
    LOG.info(value);
  }
}
