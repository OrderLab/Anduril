#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

for ((i=1;i<=3;i++)); do
  kill -9 `cat $workspace/logs-$i/pid.txt`
done
