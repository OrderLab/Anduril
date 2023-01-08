#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hdfs-4233
runtime_jar=$HOME/tmp/bytecode/$case_name/runtime-1.0-jar-with-dependencies.jar
#runtime_jar=$SCRIPT_DIR/../../tool/runtime/target/runtime-1.0-jar-with-dependencies.jar
process_number=4

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
-cp $runtime_jar \
-Dbuild.test.dir=$SCRIPT_DIR/build \
-Dlog4j.configuration=file:$SCRIPT_DIR/log4j.properties \
-DflakyAgent.distributedMode=true \
-DflakyAgent.disableAgent=true \
-Dbaseline.policy=crashtuner \
runtime.baseline.BaselineAgent $trials_dir  $trials_dir/injection-$id.json \
> $trials_dir/$id/output.txt 2>&1 &
pid=$!

HADOOP_CLASSPATH="$runtime_jar" \
INJECT_HADOOP_OPTS="-DflakyAgent.injectionPointsPath=$SCRIPT_DIR/tree.json" \
$SCRIPT_DIR/cluster/reproduce.sh

sleep 1
java -cp $runtime_jar runtime.baseline.BaselineAgent # shutdown
sleep 1

mv $SCRIPT_DIR/cluster/logs-* $trials_dir/$id

id=$(($id + 1))
if [ $id -gt 100000 ]; then
  break
fi

done
