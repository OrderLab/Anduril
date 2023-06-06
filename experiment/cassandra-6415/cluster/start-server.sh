#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

id=$1
SRC=$workspace/src-$id

CASSANDRA_HOME=$SRC \
CASSANDRA_CONF="$workspace/conf-$id" \
CASSANDRA_LOG_DIR=$workspace/logs-$id \
CASSANDRA_INCLUDE=$workspace/conf-$id/cassandra.in.sh \
$SRC/bin/cassandra -p $workspace/logs-$id/pid.txt
