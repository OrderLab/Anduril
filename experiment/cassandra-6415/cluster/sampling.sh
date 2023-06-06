#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

sampling_path=$workspace/samplings
mkdir -p $sampling_path

echo "running experiment"
$workspace/setup.sh
$workspace/start-cluster.sh
echo "started Cassandra cluster"
sleep 5
id=0
pid1=$(cat logs-1/pid.txt)
pid2=$(cat logs-2/pid.txt)
while [ $(./check-status.sh|grep "Startup"|wc -l) -lt 3 ]; do
    rm -rf $sampling_path/$id
    mkdir -p $sampling_path/$id
    jstack $pid1 > $sampling_path/$id/system1.txt
    jstack $pid2 > $sampling_path/$id/system2.txt 
    id=$(($id + 1))
    sleep 3
done
echo $id
$workspace/stop-cluster.sh

