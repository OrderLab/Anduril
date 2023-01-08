#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

#SRC=$HOME/hd-3.2.2
case_name=hdfs-13039
SRC=$workspace/../../../systems/$case_name
HADOOP_HOME=$SRC/hadoop-dist/target/hadoop-3.1.0-SNAPSHOT

LD_LIBRARY_PATH="$HADOOP_HOME/lib/native:$LD_LIBRARY_PATH"
HADOOP_HOME=$HADOOP_HOME \
HADOOP_CONF_DIR=$workspace/conf-1 \
HADOOP_PID_DIR=$workspace/logs-1 \
HADOOP_LOG_DIR=$workspace/logs-1 \
HADOOP_CLASSPATH="$HADOOP_CLASSPATH" \
$HADOOP_HOME/bin/hdfs $@
