#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

for ((i=1;i<=3;i++)); do
  CASSANDRA_OPTS="$CASSANDRA_OPTS" nohup $workspace/start-server.sh $i </dev/null >/dev/null 2>&1 &
done
