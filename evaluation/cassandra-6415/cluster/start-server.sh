#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

id=$1
case_name=cassandra-6415
SRC=$workspace/../../../systems/$case_name

CASSANDRA_HOME=$SRC \
CASSANDRA_CONF="$workspace/conf-$id" \
CASSANDRA_LOG_DIR=$workspace/logs-$id \
CASSANDRA_INCLUDE=$workspace/conf-$id/cassandra.in.sh \
$SRC/bin/cassandra -p $workspace/logs-$id/pid.txt
