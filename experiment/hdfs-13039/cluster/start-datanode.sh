#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

if [ $1 -ne 1 ] && [ $1 -ne 2 ]  && [ $1 -ne 3 ]  && [ $1 -ne 4 ]; then
  echo "exit due to wrong argument"
  exit 1
fi

case_name=hdfs-13039
SRC=$workspace/../../../systems/$case_name
HADOOP_HOME=$SRC/hadoop-dist/target/hadoop-3.1.0-SNAPSHOT
#BYTEMAN_HOME=$HOME/mcgray-detected-bugs/byteman

HADOOP_HOME=$HADOOP_HOME \
HADOOP_CONF_DIR=$workspace/conf-$1 \
HADOOP_PID_DIR=$workspace/logs-$1 \
HADOOP_LOG_DIR=$workspace/logs-$1 \
HADOOP_OPTS="$HADOOP_OPTS $BM_JVM_OPTS" \
BM_JVM_OPTS="$BM_JVM_OPTS" \
DN_BM_JVM_OPTS_SET=1 \
HADOOP_CLASSPATH="$HADOOP_CLASSPATH" \
$HADOOP_HOME/bin/hdfs --daemon start datanode
