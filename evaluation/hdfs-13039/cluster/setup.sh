#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

httpBase=19864
httpsBase=29865
addrBase=17866
ipcBase=27867

for i in 0 1 2 3 4; do
  rm -rf $workspace/conf-$i $workspace/store-$i $workspace/logs-$i
  mkdir -p $workspace/conf-$i
  mkdir -p $workspace/store-$i
  mkdir -p $workspace/logs-$i
  store_i="$workspace/store-$i"
  cp $workspace/core-site.xml $workspace/conf-$i/
  sed -i "s/replaceme/hdfs:\/\/127.0.0.1:9000/g" $workspace/conf-$i/core-site.xml
  cp $workspace/hdfs-site.xml $workspace/conf-$i/
  sed -i "s:dir_replaceme:file\://$store_i:g" $workspace/conf-$i/hdfs-site.xml
  sed -i "s/http_replaceme/127.0.0.1:$(($httpBase + $i))/g" $workspace/conf-$i/hdfs-site.xml
  sed -i "s/https_replaceme/127.0.0.1:$(($httpsBase + $i))/g" $workspace/conf-$i/hdfs-site.xml
  sed -i "s/addr_replaceme/127.0.0.1:$(($addrBase + $i))/g" $workspace/conf-$i/hdfs-site.xml
  sed -i "s/ipc_replaceme/127.0.0.1:$(($ipcBase + $i))/g" $workspace/conf-$i/hdfs-site.xml
  printf "127.0.0.1\n127.0.0.1" > $workspace/conf-$i/workers
  cp $workspace/log4j.properties $workspace/conf-$i/
done
