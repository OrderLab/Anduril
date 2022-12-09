#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hbase-19608
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

testcase="org.apache.hadoop.hbase.client.TestGetProcedureResult"

mkdir -p $SCRIPT_DIR/foo/version-2

mkdir -p $SCRIPT_DIR/build

trials_dir=$SCRIPT_DIR/trials
mkdir -p $trials_dir
id=0

echo "Experiment script pid: $$"
echo $$ >${SCRIPT_DIR}/pid.txt

while :
do
echo "Running experiment $id at $(date)"
java \
-cp $runtime_jar:$classes_dir:$jars:$SCRIPT_DIR \
-Dbuild.test.dir=$SCRIPT_DIR/build \
-Dbaseline.policy=crashtuner \
runtime.baseline.BaselineAgent $trials_dir $trials_dir/injection-$id.json org.junit.runner.JUnitCore $testcase \
> $trials_dir/output-$id.txt
sleep 1
id=$(($id + 1))
if [ $id -gt 1000000 ]; then
  break
fi
done
