#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

output=$1
shift
p1=$1
shift
p2=$1
shift
p3=$1
shift
p4=$1
shift

if [ "$p4" = "_" ]; then
  p4=
fi

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
for i in $target/lib/*.jar; do jars="$i:$jars"; done
for i in `find $HOME/.m2 -name "*.jar"|grep 'javax.json'|grep '1.1.4'`; do jars="$i:$jars"; done
for i in `find $HOME/.m2/repository/org/jboss/byteman/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done
testcase="org.apache.zookeeper.test.ZkDatabaseCorruptionTest"
byteman=""
for i in `find $HOME/.m2/repository/org/jboss/byteman/byteman/*/**.jar`; do byteman=$i; done

trials_dir=$SCRIPT_DIR/trials
mkdir -p $trials_dir
id=0

mkdir -p $SCRIPT_DIR/foo/version-2

java \
-cp $btm_dir:$classes_dir:$jars:$SCRIPT_DIR \
-Dbuild.test.dir=$SCRIPT_DIR/build \
-Dlog4j.configuration=file:$SCRIPT_DIR/log4j.properties \
-Dbaseline.policy=crashtuner \
$@ runtime.baseline.BaselineAgent $p1 $p3 $p4 org.junit.runner.JUnitCore $testcase \
> $SCRIPT_DIR/trial.out 2>&1
