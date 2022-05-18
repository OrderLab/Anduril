#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS -DflakyAgent.pid=1" $workspace/start-namenode.sh 1 1
HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS -DflakyAgent.pid=0" $workspace/start-secondarynamenode.sh 0 0
HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS -DflakyAgent.pid=2" $workspace/start-datanode.sh 2 2
HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS -DflakyAgent.pid=3" $workspace/start-datanode.sh 3 3
