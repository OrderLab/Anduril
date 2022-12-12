#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

case_name=hdfs-13039
SRC=$workspace/../../../systems/hdfs-13039
HADOOP_HOME=$SRC/hadoop-dist/target/hadoop-3.1.0-SNAPSHOT

HADOOP_HOME=$HADOOP_HOME \
HADOOP_CONF_DIR=$workspace/conf-0 \
HADOOP_PID_DIR=$workspace/logs-0 \
HADOOP_LOG_DIR=$workspace/logs-0 \
$HADOOP_HOME/bin/hdfs --daemon stop namenode
