#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

storagePortBase=7000
sslStoragePortBase=7001
nativeTransportPortBase=9047
rpcPortBase=9170
jmxPortBase=7210

# the first index is 0
tokens=("_", "-9223372036854775808", "-3074457345618258603", "3074457345618258602")
for i in 1 2 3; do
  rm -rf $workspace/conf-$i $workspace/store-$i $workspace/logs-$i
  mkdir -p $workspace/conf-$i
  mkdir -p $workspace/store-$i
  mkdir -p $workspace/logs-$i
  store_i="$workspace/store-$i"
  cp $workspace/cassandra.yaml $workspace/conf-$i/
  sed -i "s/\ \ \ \ \-\ replaceme/\ \ \ \ \-\ ${store_i//'/'/'\/'}/g" $workspace/conf-$i/cassandra.yaml
  sed -i "s/commitlog_directory\:\ replaceme/commitlog_directory\:\ ${store_i//'/'/'\/'}/g" $workspace/conf-$i/cassandra.yaml
  sed -i "s/saved_caches_directory\:\ replaceme/saved_caches_directory\:\ ${store_i//'/'/'\/'}/g" $workspace/conf-$i/cassandra.yaml
  sed -i "s/ssl_storage_port\:\ replaceme/ssl_storage_port\:\ $sslStoragePortBase/g" $workspace/conf-$i/cassandra.yaml
  sed -i "s/storage_port\:\ replaceme/storage_port\:\ $storagePortBase/g" $workspace/conf-$i/cassandra.yaml
  sed -i "s/native_transport_port\:\ replaceme/native_transport_port\:\ $nativeTransportPortBase/g" $workspace/conf-$i/cassandra.yaml
  sed -i "s/rpc_port\:\ replaceme/rpc_port\:\ $(($rpcPortBase + $i))/g" $workspace/conf-$i/cassandra.yaml
  sed -i "s/initial_token\:\ replaceme/initial_token\:\ ${tokens[$i]}/g" $workspace/conf-$i/cassandra.yaml
  sed -i "s/listen_address\:\ replaceme/listen_address\:\ 127.0.0.$i/g" $workspace/conf-$i/cassandra.yaml
  sed -i "s/rpc_address\:\ replaceme/rpc_address\:\ 127.0.0.$i/g" $workspace/conf-$i/cassandra.yaml
  cp $workspace/cassandra-env.sh $workspace/conf-$i/
  sed -i "s/JMX_PORT\=replaceme/JMX_PORT=$(($jmxPortBase + $i))/g" $workspace/conf-$i/cassandra-env.sh
  sed -i "s/JVM_EXTRA_OPTS\=replaceme/JVM_EXTRA_OPTS=\"$JVM_EXTRA_OPTS\"/g" $workspace/conf-$i/cassandra-env.sh
  cp $workspace/cassandra.in.sh $workspace/conf-$i/
  cp $workspace/hotspot_compiler $workspace/conf-$i/
  cp $workspace/logback.xml $workspace/conf-$i/
  cp $workspace/jvm.options $workspace/conf-$i/
  cp $workspace/log4j-server.properties $workspace/conf-$i/
  cp $workspace/log4j.properties $workspace/conf-$i/
  cp $runtime_jar $workspace/conf-$i/
done
