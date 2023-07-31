#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=kafka-8755
ka_dir="${SCRIPT_DIR}/../../systems/$case_name"
gr_dir="$HOME/.gradle/caches"
classes_dir="$HOME/tmp/bytecode/${case_name}/classes"
runtime_jar="$classes_dir/runtime-1.0-jar-with-dependencies.jar"
#for i in $SCRIPT_DIR/deps/*; do classes_dir="$classes_dir:$i";done
jars="$SCRIPT_DIR"
for i in `find $ka_dir -name "dependant-libs-2.12.9"`; do
  for j in `find $i -name "*.jar"`; do
    if [[ "$j" != *"/dependant-libs-2.12.9/kafka-"*".jar" ]]; then
      if [[ "$j" != *"/slf4j-log4j12-"*".jar" ]]; then
        jars="$j:$jars";
      fi
    fi
  done
done
for i in `find $ka_dir -name "dependant-libs"`; do
  for j in `find $i -name "*.jar"`; do
    if [[ "$j" != *"/dependant-libs/kafka-"*".jar" ]]; then
      if [[ "$j" != *"/slf4j-log4j12-"*".jar" ]]; then
        jars="$j:$jars";
      fi
    fi
  done
done
#Cached Dependencies
#for i in `find ~/.gradle/caches/modules-2/files-2.1/ -name "*.jar"`; do
#    jars="$i:$jars";
#done
for i in `cat $SCRIPT_DIR/extra-dependencies.txt`; do
  for j in `find $gr_dir -name $i`; do
    #echo $j
    jars="$j:$jars";
    break
  done
done

for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done

jars="$ka_dir/core/build/dependant-libs-2.12.9/slf4j-log4j12-1.7.27.jar:$jars"

testmethod="org.apache.kafka.streams.integration.StandbyTaskTest#shouldTestStandbyTask"

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
