#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hbase-25905
hb_dir="${SCRIPT_DIR}/../../systems/$case_name"
classes_dir="$HOME/tmp/bytecode/${case_name}/classes"
runtime_jar="$classes_dir/runtime-1.0-jar-with-dependencies.jar"
for i in $SCRIPT_DIR/deps/*; do classes_dir="$classes_dir:$i";done
jars="$SCRIPT_DIR"
for i in `head -n1 $hb_dir/hbase-build-configuration/target/cached_classpath.txt|tr ':' '\n'`; do
  if [[ "$i" == *"hbase"* ]]; then
    if [[ "$i" == *"thirdparty"* ]]; then
      jars="$i:$jars"
    fi
  else
    jars="$i:$jars"
  fi
done
for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done

testcase="org.apache.hadoop.hbase.replication.TestReplicationSmallTests"


trials_dir=$SCRIPT_DIR/tmp_trials

rm -rf $trials_dir
mkdir -p $trials_dir


java -noverify \
-Dlog4j.configurationFile=file:$SCRIPT_DIR/log4j2.xml \
-cp $classes_dir:$jars:$runtime_jar \
-DflakyAgent.timeTraceCollectMode=true \
-DflakyAgent.logInject=false \
-DflakyAgent.fixPointInjectionMode=true \
-DflakyAgent.injectionId=1 \
-DflakyAgent.injectionTimes=-1 \
-DflakyAgent.fault=java.io.IOException \
-DflakyAgent.traceFile=$SCRIPT_DIR/trace.txt \
runtime.TraceAgent $trials_dir $SCRIPT_DIR/tree.json $trials_dir/injection-$id.json org.junit.runner.JUnitCore $testcase 

