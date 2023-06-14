#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
output=$1
shift
p1=$1
shift
p2=$1
shift
p3=$1
shift
p4=$1
shift

if [ "$p4" = "_" ]; then
  p4=
fi

case_name=cassandra-6415
runtime_jar=$HOME/tmp/bytecode/$case_name/runtime-1.0-jar-with-dependencies.jar
#runtime_jar=$SCRIPT_DIR/../../tool/runtime/target/runtime-1.0-jar-with-dependencies.jar


java  -cp $runtime_jar \
-Dlog4j.configuration=file:$SCRIPT_DIR/log4j.properties \
$@ runtime.TraceAgent $p1 $p2 $p3 $p4 \
> $SCRIPT_DIR/output.txt 2>&1 &
pid=$!

sleep 300

tree_json="$SCRIPT_DIR/tree.json"
tree_json=$(echo $tree_json | sed 's/\//\\\//g')
CASSANDRA_CLASSPATH="$runtime_jar" \
INJECT_CASSANDRA_OPTS="-DflakyAgent.injectionPointsPath=$tree_json" \
$SCRIPT_DIR/cluster/reproduction.sh > $SCRIPT_DIR/workload-output.log 2>&1

sleep 1
java -jar $runtime_jar # shutdown
sleep 1


