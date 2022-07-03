#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hdfs-12248
hd_dir="${SCRIPT_DIR}/../../systems/$case_name"
classes_dir="$HOME/tmp/bytecode/${case_name}/classes"
runtime_jar="$classes_dir/runtime-1.0-jar-with-dependencies.jar"
jars="$SCRIPT_DIR"
for i in `find $hd_dir/hadoop-common-project/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $hd_dir/hadoop-hdfs-project/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done
for i in $hd_dir/hadoop-tools/hadoop-distcp/target/lib/*.jar; do jars="$i:$jars"; done
testcase="org.apache.hadoop.hdfs.TestRollingUpgrade"

trials_dir=$SCRIPT_DIR/trials
rm -rf $trials_dir
mkdir -p $trials_dir
id=0

echo "Experiment script pid: $$"
echo $$ >${SCRIPT_DIR}/pid.txt

while :
do
echo "Running experiment $id at $(date)"
java \
-cp $classes_dir:$jars:$runtime_jar \
-Dlog4j.configuration=file:$SCRIPT_DIR/log4j.properties \
-Dbaseline.policy=exhaustive \
runtime.baseline.BaselineAgent $trials_dir $trials_dir/injection-$id.json org.junit.runner.JUnitCore $testcase \
> $trials_dir/output-$id.txt 2>&1
sleep 1
id=$(($id + 1))
if [ $id -gt 1000000 ]; then
  break
fi
done
