#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

HADOOP_OPTS="$HADOOP_OPTS" $workspace/start-namenode.sh 1 1
HADOOP_OPTS="$HADOOP_OPTS" $workspace/start-secondarynamenode.sh 0 0
HADOOP_OPTS="$HADOOP_OPTS" $workspace/start-datanode.sh 2 2
HADOOP_OPTS="$HADOOP_OPTS" $workspace/start-datanode.sh 3 3
