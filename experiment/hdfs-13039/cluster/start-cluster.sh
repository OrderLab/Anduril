#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS -DflakyAgent.pid=0" $workspace/start-namenode.sh
#./client.sh ec -enablePolicy -policy XOR-2-1-1024k
HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS -DflakyAgent.pid=1" $workspace/start-datanode.sh 1
HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS -DflakyAgent.pid=2" $workspace/start-datanode.sh 2
HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS -DflakyAgent.pid=3" $workspace/start-datanode.sh 3
HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS -DflakyAgent.pid=4" $workspace/start-datanode.sh 4
