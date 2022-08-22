#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hdfs-13039
runtime_jar=$HOME/tmp/bytecode/$case_name/runtime-1.0-jar-with-dependencies.jar
#runtime_jar=$SCRIPT_DIR/../../tool/runtime/target/runtime-1.0-jar-with-dependencies.jar
process_number=5

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
mkdir -p $trials_dir/$id

java \
-Dlog4j.configuration=file:$SCRIPT_DIR/log4j.properties \
-DflakyAgent.recordOnthefly=true \
-DflakyAgent.logInject=false \
-DflakyAgent.avoidBlockMode=true \
-DflakyAgent.injectionOccurrenceLimit=3 \
-DflakyAgent.slidingWindow=10 \
-DflakyAgent.feedback=true \
-DflakyAgent.distributedMode=true \
-DflakyAgent.disableAgent=true \
-DflakyAgent.traceFile=$trials_dir/trace-$id.txt \
-jar $runtime_jar $process_number $trials_dir $SCRIPT_DIR/tree.json $trials_dir/injection-$id.json \
> $trials_dir/$id/output.txt 2>&1 &
pid=$!

sleep 2

HADOOP_CLASSPATH="$runtime_jar" \
INJECT_HADOOP_OPTS="-DflakyAgent.injectionPointsPath=$SCRIPT_DIR/tree.json" \
$SCRIPT_DIR/cluster/reproduction.sh > $trials_dir/$id/workload-output.log 2>&1

sleep 1
java -jar $runtime_jar # shutdown
sleep 1

feedback="$($NODEJS $SCRIPT_DIR/diff-score.js $GROUND_TRUTH/good-run-log $SCRIPT_DIR/cluster $GROUND_TRUTH/diff_log.txt $SCRIPT_DIR/tree.json | paste -sd ' ' - )"
sed -i "3 i \ \ \ \ \"feedback\"\:\ $feedback," $SCRIPT_DIR/trials/injection-$id.json
mv $SCRIPT_DIR/cluster/logs-* $trials_dir/$id

id=$(($id + 1))
if [ $id -gt 100000 ]; then
  break
fi

done
