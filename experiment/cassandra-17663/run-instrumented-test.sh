#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=cassandra-17663
ca_dir="${SCRIPT_DIR}/../../systems/$case_name"
#rm -r original.txt
#rm -r test.txt
#for i in `find $ca_dir -name "*.class"`; do 
#    if [[ "$i" == *"/build/classes/main/"* ]]; then
#        p=$(echo ${i#*main/})
#        echo $p >> original.txt  
#    fi
#    if [[ "$i" == *"/build/test/classes/"* ]]; then
#        p=$(echo ${i#*classes/})
#        echo $p >> test.txt
#    fi
#done
classes_dir="$HOME/tmp/bytecode/${case_name}/classes"
runtime_jar="$classes_dir/runtime-1.0-jar-with-dependencies.jar"
#If not add, log for nodes can not be created
#classes_dir="$classes_dir:$ca_dir/build/test/classes"
#classes_dir="$ca_dir/build/test/classes:$classes_dir"
for i in $SCRIPT_DIR/deps/*; do classes_dir="$classes_dir:$i";done
jars="$SCRIPT_DIR"
for i in `find $ca_dir/build/lib/jars/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $ca_dir/build/test/lib/jars/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done
testcase="org.apache.cassandra.distributed.test.RepairErrorsTest"

if [ -z "$(which node)" ]; then
  if [ -f "$HOME/nodejs-install/bin/node" ]; then
    NODEJS=$HOME/nodejs-install/bin/node
  fi
else
  NODEJS=node
fi
GROUND_TRUTH=$SCRIPT_DIR/../../ground_truth/$case_name

java -noverify \
-cp $classes_dir:$jars:$runtime_jar \
-DflakyAgent.logInject=true \
-DflakyAgent.fixPointInjectionMode=true \
-DflakyAgent.injectionId=-1 \
-DflakyAgent.injectionTimes=-1 \
-DflakyAgent.fault=java.io.IOException \
-DflakyAgent.traceFile=$SCRIPT_DIR/trace.txt \
org.junit.runner.JUnitCore $testcase
