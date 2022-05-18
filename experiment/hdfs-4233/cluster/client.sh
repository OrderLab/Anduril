#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

SRC=$workspace/../../../systems/hdfs-4233
HADOOP_HOME=$SRC/hadoop-dist/target/hadoop-0.23.6-SNAPSHOT

HADOOP_HOME=$HADOOP_HOME \
HADOOP_CONF_DIR=$workspace/conf-1 \
HADOOP_PID_DIR=$workspace/logs-1 \
HADOOP_LOG_DIR=$workspace/logs-1 \
HADOOP_OPTS="$HADOOP_OPTS" \
HADOOP_CLASSPATH="$HADOOP_CLASSPATH" \
$HADOOP_HOME/bin/hdfs dfs $@
