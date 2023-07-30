#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=kafka-10048
ka_dir="${SCRIPT_DIR}/../../systems/$case_name"
classes_dir="$HOME/tmp/bytecode/${case_name}/classes"
runtime_jar="$classes_dir/runtime-1.0-jar-with-dependencies.jar"
for i in $SCRIPT_DIR/deps/*; do classes_dir="$classes_dir:$i";done
jars="$SCRIPT_DIR"
for i in `find $ka_dir -name "dependant-libs-2.13.3"`; do
  for j in `find $i -name "*.jar"`; do
    if [[ $j" != *"-SNAPSHOT.jar ]]; then
      if [[ "$j" != *"/slf4j-log4j12-1.7.30.jar" ]]; then
	if [[ "$j" != *"/slf4j-api-1.7.30.jar" ]]; then
          jars="$j:$jars";
	fi 
      fi
    fi
  done
done

for i in `find $ka_dir -name "dependant-libs"`; do
  for j in `find $i -name "*.jar"`; do
    if [[ $j" != *"-SNAPSHOT.jar ]]; then
      if [[ "$j" != *"/slf4j-log4j12-1.7.30.jar" ]]; then
        if [[ "$j" != *"/slf4j-api-1.7.30.jar" ]]; then
          jars="$j:$jars";
        fi
      fi
    fi
  done
done

for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done

jars="$ka_dir/core/build/dependant-libs-2.13.3/slf4j-log4j12-1.7.30.jar:$ka_dir/core/build/dependant-libs-2.13.3/slf4j-api-1.7.30.jar:$jars"
#jars="${SCRIPT_DIR}/slf4j-api-1.7.25.jar"
testmethod="org.apache.kafka.connect.mirror.MirrorConnectorsIntegrationTest#testReplicationWithEmptyPartition"

if [ -z "$(which node)" ]; then
  if [ -f "$HOME/nodejs-install/bin/node" ]; then
    NODEJS=$HOME/nodejs-install/bin/node
  fi
else
  NODEJS=node
fi
GROUND_TRUTH=$SCRIPT_DIR/../../ground_truth/$case_name

trials_dir=$SCRIPT_DIR/tmp_trials
rm -rf $trials_dir
mkdir -p $trials_dir

java -noverify \
-DflakyAgent.timeTraceCollectMode=true \
-DflakyAgent.logInject=false \
-DflakyAgent.fixPointInjectionMode=true \
-DflakyAgent.injectionId=1 \
-DflakyAgent.injectionTimes=-1 \
-DflakyAgent.fault=java.io.IOException \
-DflakyAgent.traceFile=$SCRIPT_DIR/trace.txt \
-cp $classes_dir:$jars:$runtime_jar:$SCRIPT_DIR/junit-platform-console-standalone-1.7.0.jar \
runtime.TraceAgent $trials_dir $SCRIPT_DIR/tree.json $SCRIPT_DIR/injection.json  org.junit.platform.console.ConsoleLauncher --select-method $testmethod
