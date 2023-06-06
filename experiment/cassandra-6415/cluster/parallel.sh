#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

for ((i=1;i<=3;i++)); do
  nohup $workspace/workload.sh $i read 5 </dev/null >$workspace/client-read-$i.out 2>&1 &
  nohup $workspace/workload.sh $i write 5 </dev/null >$workspace/client-write-$i.out 2>&1 &
done
