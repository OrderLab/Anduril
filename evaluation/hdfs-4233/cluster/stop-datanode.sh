#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

id=$1
lid=$2

if [ $id -ne 2 ] && [ $id -ne 3 ]; then
  echo "exit due to wrong argument"
  exit 1
fi

SRC=$workspace/../../../systems/hdfs-4233
HADOOP_HOME=$SRC/hadoop-dist/target/hadoop-0.23.6-SNAPSHOT

HADOOP_HOME=$HADOOP_HOME \
HADOOP_CONF_DIR=$workspace/conf-$id \
HADOOP_PID_DIR=$workspace/logs-$lid \
HADOOP_LOG_DIR=$workspace/logs-$lid \
HADOOP_CLASSPATH="$HADOOP_CLASSPATH" \
$HADOOP_HOME/sbin/hadoop-daemon.sh stop datanode
