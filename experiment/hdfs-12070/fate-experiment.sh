#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hdfs-12070
hd_dir="${SCRIPT_DIR}/../../systems/$case_name"
classes_dir="$HOME/tmp/bytecode/${case_name}/classes"
runtime_jar="$classes_dir/runtime-1.0-jar-with-dependencies.jar"
jars="$SCRIPT_DIR"
for i in `find $hd_dir/hadoop-common-project/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $hd_dir/hadoop-hdfs-project/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done
for i in $hd_dir/hadoop-tools/hadoop-distcp/target/lib/*.jar; do jars="$i:$jars"; done
testcase="org.apache.hadoop.hdfs.TestLeaseRecovery"


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
runtime.fate.FateAgent $trials_dir $trials_dir/injection-$id.json org.junit.runner.JUnitCore $testcase \
> $trials_dir/output-$id.txt
sleep 1
id=$(($id + 1))
if [ $id -gt 1000000 ]; then
  break
fi
done

