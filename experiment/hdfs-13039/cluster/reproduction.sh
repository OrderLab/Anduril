#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)
./setup.sh
./format.sh
./start-cluster.sh
./client.sh dfsadmin -safemode leave
./client.sh dfs -mkdir /1
./client.sh ec -setPolicy -path /1  -policy XOR-2-1-1024k
./workload.sh reproduce /1/test > client.log 2>&1
target_line=`grep 'Corruption Target is' client.log`
target=$(echo ${target_line#*is })
dir=$(expr $target - 27867)
pid_file=`find logs-$dir -name *.pid`
kill -9 `cat $pid_file`
sleep 45
./workload.sh write /2/test 100 > output.log 2>&1
./stop-cluster.sh
