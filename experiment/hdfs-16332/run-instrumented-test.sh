#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hdfs-16332
hd_dir="${SCRIPT_DIR}/../../systems/$case_name"
classes_dir="$HOME/tmp/bytecode/${case_name}/classes"
runtime_jar="$classes_dir/runtime-1.0-jar-with-dependencies.jar"
for i in $SCRIPT_DIR/deps/*; do classes_dir="$classes_dir:$i";done
jars="$SCRIPT_DIR"
#for i in `find $hd_dir/hadoop-common-project/ -name "*.jar"`; do jars="$i:$jars"; done
#for i in `find $hd_dir/hadoop-hdfs-project/ -name "*.jar"`; do jars="$i:$jars"; done
for i in $hd_dir/hadoop-tools/hadoop-distcp/target/lib/*.jar; do jars="$i:$jars"; done
for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done

testcase="org.apache.hadoop.hdfs.protocol.datatransfer.sasl.TestSaslDataTransferExpiredBlockToken"

if [ -z "$(which node)" ]; then
  if [ -f "$HOME/nodejs-install/bin/node" ]; then
    NODEJS=$HOME/nodejs-install/bin/node
  fi
else
  NODEJS=node
fi
GROUND_TRUTH=$SCRIPT_DIR/../../ground_truth/$case_name

java -noverify \
-Dlog4j.configuration=file:$SCRIPT_DIR/log4j.properties \
-cp $classes_dir:$jars:$runtime_jar \
-DflakyAgent.logInject=true \
-DflakyAgent.fixPointInjectionMode=true \
-DflakyAgent.injectionId=1 \
-DflakyAgent.injectionTimes=-1 \
-DflakyAgent.fault=java.io.IOException \
-DflakyAgent.traceFile=$SCRIPT_DIR/trace.txt \
org.junit.runner.JUnitCore $testcase
