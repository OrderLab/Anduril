#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

INJECT_HADOOP_OPTS="-DflakyAgent.distributedMode=true -DflakyAgent.disableAgent=false $INJECT_HADOOP_OPTS"
HADOOP_OPTS="-DflakyAgent.distributedMode=true -DflakyAgent.disableAgent=true"
HADOOP_CLASSPATH="$HADOOP_CLASSPATH"

$workspace/setup.sh
cp -r $workspace/init-store/current $workspace/store-1/

HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$INJECT_HADOOP_OPTS" $workspace/start-cluster.sh

for i in 0 1 2 3; do
  pid=`cat $workspace/logs-$i/*.pid`
  if [ $(ps -p $pid | wc -l) -eq 1 ]; then
    echo "some node fails to start"
    HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS" $workspace/stop-cluster.sh
    exit 0
  fi
done

sleep 2

# $workspace/injection_client.sh

for i in 1 2 3 4; do
  pid=`cat $workspace/logs-1/*.pid`
  if [ $(ps -p $pid | wc -l) -eq 1 ]; then
    echo "namenode failed"
    HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS" $workspace/stop-cluster.sh
    exit 0
  fi
  HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS" $workspace/client.sh -mkdir /$i &
  pid=$!
  for j in {0..5}; do
    sleep 1
    if [ $(ps -p $pid | wc -l) -eq 1 ]; then
      break
    fi
  done
  if [ $(ps -p $pid | wc -l) -gt 1 ]; then
    echo "some client gets stuck"
    kill -9 $pid
    HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS" $workspace/stop-cluster.sh
    exit 0
  fi
done

pid=`cat $workspace/logs-1/*.pid`
for i in {0..150}; do
  if [ $(ps -p $pid | wc -l) -eq 1 ]; then
    echo "namenode failed"
    HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS" $workspace/stop-cluster.sh
    exit 0
  fi
  if [ $(grep 'Rolling edit logs' $workspace/logs-1/*.log | wc -l) -gt 0 ]; then
    echo "finish rolling edit logs"
    break
  fi
  sleep 1
done

sleep 3

HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS" $workspace/stop-cluster.sh
