#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# Do not use Oracle JVM
if [ $(java -version 2>&1 | grep -ic openjdk) == 0 ]; then
  JAVA_HOME=$HOME/java-8-openjdk-amd64
fi

case_name=zookeeper-3006
zk_dir="${SCRIPT_DIR}/../../systems/$case_name"
btm_dir="${SCRIPT_DIR}/none"
target="$zk_dir"/zookeeper-server/target
classes_dir="$HOME/tmp/bytecode/${case_name}/classes"

jars="."
for i in $classes_dir/*.jar; do jars="$i:$jars"; done
for i in $target/lib/*.jar; do jars="$i:$jars"; done
for i in `find $HOME/.m2 -name "*.jar"|grep 'javax.json'|grep '1.1.4'`; do jars="$i:$jars"; done
for i in `find $HOME/.m2/repository/org/jboss/byteman/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done
testcase="org.apache.zookeeper.test.ZkDatabaseCorruptionTest"
byteman=""
for i in `find $HOME/.m2/repository/org/jboss/byteman/byteman/*/**.jar`; do byteman=$i; done

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
-cp $btm_dir:$classes_dir:$jars:$SCRIPT_DIR \
-Dbuild.test.dir=$SCRIPT_DIR/build \
runtime.stacktrace.StacktraceAgent $trials_dir $SCRIPT_DIR/stacktrace.json $trials_dir/injection-$id.json org.junit.runner.JUnitCore $testcase \
> $trials_dir/output-$id.txt
sleep 1
id=$(($id + 1))
if [ $id -gt 1000000 ]; then
  break
fi
done

