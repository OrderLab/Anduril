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

trial_timeout=180

echo "Experiment script pid: $$"
echo $$ >${SCRIPT_DIR}/pid.txt

while :
do
echo "Running experiment $id at $(date)"
rm -rf *.log
rm -rf *.log.*
rm -rf build
java -noverify \
-cp $classes_dir:$jars:$runtime_jar \
-DflakyAgent.recordOnthefly=true \
-DflakyAgent.logInject=false \
-DflakyAgent.avoidBlockMode=true \
-DflakyAgent.injectionOccurrenceLimit=10000000 \
-DflakyAgent.slidingWindow=10000000 \
-DflakyAgent.trialTimeout=$trial_timeout \
-DflakyAgent.feedback=false \
-DflakyAgent.traceFile=$trials_dir/trace-$id.txt \
runtime.TraceAgent $trials_dir $SCRIPT_DIR/tree.json $trials_dir/injection-$id.json org.junit.runner.JUnitCore $testcase \
> $trials_dir/output-$id.txt 2>&1 &
pid=$!
pid_alive=1
for ((s = 0; s < trial_timeout + 5; s++)); do
  if [ $(ps -p $pid | wc -l) -eq 1 ]; then
    pid_alive=0
    break
  fi
  sleep 1
done
if [ $pid_alive -eq 1 ]; then
  kill -9 $pid
fi
sleep 1
#feedback="$($NODEJS $SCRIPT_DIR/diff-score.js $GROUND_TRUTH/good-run-log.txt $SCRIPT_DIR/trials/output-$id.txt $GROUND_TRUTH/diff_log.txt $SCRIPT_DIR/tree.json | paste -sd ' ' - )"
#sed -i "3 i \ \ \ \ \"feedback\"\:\ $feedback," $SCRIPT_DIR/trials/injection-$id.json
id=$(($id + 1))
if [ $id -gt 1000000 ]; then
  break
fi
done
