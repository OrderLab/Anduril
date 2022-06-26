#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hbase-20492
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

testcase="org.apache.hadoop.hbase.master.assignment.TestUnexpectedStateException"

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
java -noverify \
-Dlog4j.configuration=file:$SCRIPT_DIR/log4j.properties \
-cp $classes_dir:$jars:$runtime_jar \
-DflakyAgent.avoidBlockMode=true \
-DflakyAgent.trialTimeout=120 \
-DflakyAgent.injectionOccurrenceLimit=3 \
-DflakyAgent.feedback=true \
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
