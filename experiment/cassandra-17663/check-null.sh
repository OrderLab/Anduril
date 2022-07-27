#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=cassandra-17663
ca_dir="${SCRIPT_DIR}/../../systems/$case_name"
classes_dir="$HOME/tmp/bytecode/${case_name}/classes"
runtime_jar="$classes_dir/runtime-1.0-jar-with-dependencies.jar"
#classes_dir="$ca_dir/build/test/classes:$classes_dir"
for i in $SCRIPT_DIR/deps/*; do classes_dir="$classes_dir:$i";done
jars="$SCRIPT_DIR"
for i in `find $ca_dir/build/lib/jars/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $ca_dir/build/test/lib/jars/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done
testcase="org.apache.cassandra.distributed.test.RepairErrorsTest"

trials_dir=$SCRIPT_DIR/trials
rm -rf $trials_dir
mkdir -p $trials_dir

id=0
rm -rf $SCRIPT_DIR/build
java -noverify \
-cp $classes_dir:$jars:$runtime_jar \
-DflakyAgent.logInject=true \
-DflakyAgent.fixPointInjectionMode=true \
-DflakyAgent.injectionId=-1 \
-DflakyAgent.injectionTimes=-1 \
-DflakyAgent.fault=java.io.IOException \
-DflakyAgent.avoidBlockMode=true \
-DflakyAgent.injectionOccurrenceLimit=10000000 \
-DflakyAgent.slidingWindow=10000000 \
-DflakyAgent.trialTimeout=180 \
-DflakyAgent.feedback=false \
-DflakyAgent.traceFile=$trials_dir/trace-$id.txt \
runtime.TraceAgent $trials_dir $SCRIPT_DIR/tree.json $trials_dir/injection-$id.json org.junit.runner.JUnitCore $testcase 
