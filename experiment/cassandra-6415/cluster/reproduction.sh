#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)
case_name=cassandra-6415
runtime_jar=$HOME/tmp/bytecode/$case_name/runtime-1.0-jar-with-dependencies.jar

INJECT_CASSANDRA_OPTS="-DflakyAgent.distributedMode=true -DflakyAgent.disableAgent=false $INJECT_CASSANDRA_OPTS"
CASSANDRA_OPTS="-DflakyAgent.distributedMode=true -DflakyAgent.disableAgent=false"
CASSANDRA_CLASSPATH="$runtime_jar"

kill_all () {
  local pids=$(jps | grep $1 | cut -d " " -f 1)
  if [ ! -z "$pids" ]; then
    kill -9 $pids
    echo "Bad Thing Happens"
  fi
}

wait_all () {
  id=0
  while [ $(jps|grep $1|wc -l) -gt 0 ]; do
    sleep 1
    id=$(($id + 1))
    if [ $id -gt $2 ]; then
      break
    fi
  done
}

JVM_EXTRA_OPTS="$INJECT_CASSANDRA_OPTS" runtime_jar="$runtime_jar"  $workspace/setup.sh
CASSANDRA_CLASSPATH="$CASSANDRA_CLASSPATH" CASSANDRA_OPTS="$INJECT_CASSANDRA_OPTS" $workspace/start-cluster.sh
sleep 1
while [ $(./check-status.sh|grep "Startup"|wc -l) -lt 2 ]; do
  sleep 1
done
echo "started Cassandra cluster"
$workspace/create.sh
echo "Finish Create"
wait_all "CassandraClientMain" "30"
sleep 1
$workspace/parallel.sh
echo "Read and write started"
sleep 2
#wait_all "CassandraClientMain" "60"
#kill_all "CassandraClientMain" 
# Simulate the faults by killing the node3
#pid_file=$workspace/logs-3/pid.txt
#kill -9 `cat $pid_file`
#sleep 1
nohup $workspace/src-1/bin/nodetool -h 127.0.1 -p 7211 repair > extra-output.txt 2>&1 &
echo "Started repair"
wait_all "NodeCmd" "20"
kill_all "NodeCmd"
jstack `cat $workspace/logs-1/pid.txt` > stack_trace.txt
CASSANDRA_CLASSPATH="$CASSANDRA_CLASSPATH" CASSANDRA_OPTS="$INJECT_CASSANDRA_OPTS" $workspace/stop-cluster.sh
