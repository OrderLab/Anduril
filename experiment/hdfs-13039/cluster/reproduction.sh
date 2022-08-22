#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)
case_name=hdfs-13039
runtime_jar=$HOME/tmp/bytecode/$case_name/runtime-1.0-jar-with-dependencies.jar

INJECT_HADOOP_OPTS="-DflakyAgent.distributedMode=true -DflakyAgent.disableAgent=false $INJECT_HADOOP_OPTS"
HADOOP_OPTS="-DflakyAgent.distributedMode=true -DflakyAgent.disableAgent=false"
HADOOP_CLASSPATH="$runtime_jar"


$workspace/setup.sh
#HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$INJECT_HADOOP_OPTS" ./format.sh
cp -r $workspace/init-store/current $workspace/store-0/
HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$INJECT_HADOOP_OPTS" $workspace/start-cluster.sh

for i in 0 1 2 3 4; do
  pid=`cat $workspace/logs-$i/*.pid`
  if [ $(ps -p $pid | wc -l) -eq 1 ]; then
    echo "some node fails to start"
    HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS" $workspace/stop-cluster.sh
    exit 0
  fi
done

sleep 2

#HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$INJECT_HADOOP_OPTS" ./client.sh dfsadmin -safemode leave
#HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$INJECT_HADOOP_OPTS" ./client.sh dfs -mkdir /1
#HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$INJECT_HADOOP_OPTS" ./client.sh ec -setPolicy -path /  -policy XOR-2-1-1024k
$workspace/workload.sh reproduce /1/test > $workspace/client.log 2>&1
target_line=`grep 'Corruption Target is' $workspace/client.log`
target=$(echo ${target_line#*is })
dir=$(expr $target - 27867)
pid_file=`find $workspace/logs-$dir -name *.pid`
kill -9 `cat $pid_file`
sleep 45
$workspace/workload.sh write /2/test 3 
HADOOP_CLASSPATH="$HADOOP_CLASSPATH" HADOOP_OPTS="$HADOOP_OPTS" $workspace/stop-cluster.sh
