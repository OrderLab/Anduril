#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

id=0
SRC=$workspace/src

CASSANDRA_HOME=$SRC \
CASSANDRA_CONF="$workspace/conf-$id" \
CASSANDRA_LOG_DIR=$workspace/logs-$id \
CASSANDRA_INCLUDE=$workspace/conf-$id/cassandra.in.sh \
nohup $SRC/bin/nodetool -h 127.0.1 -p 7211 repair > extra-output.txt 2>&1 &
