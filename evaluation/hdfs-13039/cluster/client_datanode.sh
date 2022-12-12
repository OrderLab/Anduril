#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

#SRC=$HOME/hd-3.2.2
SRC=$workspace/hadoop
HADOOP_HOME=$SRC/hadoop-dist/target/hadoop-3.0.0-alpha3-SNAPSHOT

HADOOP_HOME=$HADOOP_HOME \
HADOOP_CONF_DIR=$workspace/conf-3 \
HADOOP_PID_DIR=$workspace/logs-3 \
HADOOP_LOG_DIR=$workspace/logs-3 \
$HADOOP_HOME/bin/hdfs $@
