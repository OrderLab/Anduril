#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=zookeeper-3157
zk_dir="${SCRIPT_DIR}/../../systems/$case_name"
#btm_dir="${SCRIPT_DIR}/none"
target="$zk_dir"/build
classes_dir="$HOME/tmp/bytecode/$case_name/classes"
#runtime_classes_dir="$SCRIPT_DIR/runtime-classes"
#testclasses_dir="$target/test-classes"
jars="."
for i in $target/lib/*.jar; do jars="$i:$jars"; done
for i in $target/test/lib/*.jar; do jars="$i:$jars"; done
for i in `find $HOME/.m2 -name "*.jar"|grep 'javax.json'|grep '1.1.4'`; do jars="$i:$jars"; done
#for i in `find $HOME/.m2/repository/org/jboss/byteman/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done
testcase="org.apache.zookeeper.server.quorum.FuzzySnapshotRelatedTest"
#byteman=""
#for i in `find $HOME/.m2/repository/org/jboss/byteman/byteman/*/**.jar`; do byteman=$i; done

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
-cp $classes_dir:$jars:$SCRIPT_DIR \
-Dbuild.test.dir=$SCRIPT_DIR/build \
runtime.fate.FateAgent $trials_dir $trials_dir/injection-$id.json org.junit.runner.JUnitCore $testcase \
> $trials_dir/output-$id.txt
sleep 1
id=$(($id + 1))
if [ $id -gt 1000000 ]; then
  break
fi
done
