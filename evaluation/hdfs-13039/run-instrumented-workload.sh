#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=${PWD##*/}                    # to assign to a variable
case_name=${case_name:-/}               # to correct for the case where PWD=/

case_name=$(printf '%s\n' "${PWD##*/}") # to print to stdout
                                        # ...more robust than echo for unusual names
                                        #    (consider a directory named -e or -n)

runtime_jar=$HOME/tmp/bytecode/$case_name/runtime-1.0-jar-with-dependencies.jar
process_number=5
trials_dir=$SCRIPT_DIR/tmp_trials

rm -rf $trials_dir
mkdir -p $trials_dir

java \
-Dlog4j.configuration=file:$SCRIPT_DIR/log4j.properties \
-DflakyAgent.logInject=true \
-DflakyAgent.fixPointInjectionMode=true \
-DflakyAgent.injectionId=1 \
-DflakyAgent.injectionTimes=-1 \
-DflakyAgent.fault=java.io.IOException \
-DflakyAgent.distributedMode=true \
-DflakyAgent.disableAgent=true \
-DflakyAgent.traceFile=$trials_dir/trace.txt \
-jar $runtime_jar $process_number $trials_dir $SCRIPT_DIR/tree.json $SCRIPT_DIR/injection.json \
> $SCRIPT_DIR/output.txt 2>&1 &
pid=$!

sleep 20

HADOOP_CLASSPATH="$runtime_jar" \
INJECT_HADOOP_OPTS="-DflakyAgent.logInject=true -DflakyAgent.injectionPointsPath=$SCRIPT_DIR/tree.json" \
$SCRIPT_DIR/cluster/reproduction.sh

sleep 1
java -jar $runtime_jar # shutdown
sleep 1

rm -rf $trials_dir

rm -rf $SCRIPT_DIR/record-inject
mkdir -p $SCRIPT_DIR/record-inject
for ((i=0;i<process_number;i++)); do
  mkdir -p $SCRIPT_DIR/record-inject/logs-$i
  cp $SCRIPT_DIR/cluster/logs-$i/*.log $SCRIPT_DIR/record-inject/logs-$i/
done

