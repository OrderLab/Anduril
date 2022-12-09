#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=kafka-9374
ka_dir="${SCRIPT_DIR}/../../systems/$case_name"
classes_dir="$HOME/tmp/bytecode/${case_name}/classes"
runtime_jar="$classes_dir/runtime-1.0-jar-with-dependencies.jar"
#for i in $SCRIPT_DIR/deps/*; do classes_dir="$classes_dir:$i";done
jars="$SCRIPT_DIR"
for i in `find $ka_dir -name "dependant-libs-2.13.2"`; do
  for j in `find $i -name "*.jar"`; do
    if [[ "$j" != *"-SNAPSHOT.jar" ]]; then
      if [[ "$j" != *"/slf4j-log4j12-1.7.30.jar" ]]; then
        jars="$j:$jars";
      fi
    fi
  done
done
for i in `find $ka_dir -name "dependant-libs"`; do
  for j in `find $i -name "*.jar"`; do
    if [[ "$j" != *"-SNAPSHOT.jar" ]]; then
      if [[ "$j" != *"/slf4j-log4j12-1.7.30.jar" ]]; then
        jars="$j:$jars";
      fi
    fi
  done
done

for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done

for i in `cat $SCRIPT_DIR/extra-dependencies.txt`; do
  q=$(echo "$i"|sed "s#/home/tonypan#$HOME#")
  jars="$q:$jars";
done

testcase="org.apache.kafka.connect.integration.BlockingConnectorTest"

if [ -z "$(which node)" ]; then
  if [ -f "$HOME/nodejs-install/bin/node" ]; then
    NODEJS=$HOME/nodejs-install/bin/node
  fi
else
  NODEJS=node
fi
GROUND_TRUTH=$SCRIPT_DIR/../../ground_truth/$case_name

trials_dir=$SCRIPT_DIR/trials
rm -rf $trials_dir
mkdir -p $trials_dir
id=0

echo "Experiment script pid: $$"
echo $$ >${SCRIPT_DIR}/pid.txt

while :
do
echo "Running experiment $id at $(date)"
rm -rf *.log
rm -rf *.log.*
java -noverify \
-Dlog4j.configuration=file:$SCRIPT_DIR/log4j.properties \
-cp $classes_dir:$jars:$runtime_jar \
-DflakyAgent.avoidBlockMode=true \
-DflakyAgent.injectionOccurrenceLimit=10000000 \
-DflakyAgent.slidingWindowSize=10000000 \
-DflakyAgent.feedback=false \
-DflakyAgent.traceFile=$trials_dir/trace-$id.txt \
runtime.TraceAgent $trials_dir $SCRIPT_DIR/tree.json $trials_dir/injection-$id.json org.junit.runner.JUnitCore $testcase \
> $trials_dir/output-$id.txt 2>&1
sleep 1
feedback="$($NODEJS $SCRIPT_DIR/diff-score.js $GROUND_TRUTH/good-run-log.txt $SCRIPT_DIR/trials/output-$id.txt $GROUND_TRUTH/diff_log.txt $SCRIPT_DIR/tree.json | paste -sd ' ' - )"
sed -i "3 i \ \ \ \ \"feedback\"\:\ $feedback," $SCRIPT_DIR/trials/injection-$id.json
id=$(($id + 1))
if [ $id -gt 1000000 ]; then
  break
fi
done
