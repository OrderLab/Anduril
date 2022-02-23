#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

zk_dir="${SCRIPT_DIR}/../../systems/zookeeper-3157"
#btm_dir="${SCRIPT_DIR}/none"
target="$zk_dir"/build/
classes_dir="$target/classes"
#runtime_classes_dir="$SCRIPT_DIR/runtime-classes"
testclasses_dir="$target/test/classes"
jars="."
for i in $target/lib/*.jar; do jars="$i:$jars"; done
for i in $target/test/lib/*.jar; do jars="$i:$jars"; done
#for i in `find $HOME/.m2/repository/org/jboss/byteman/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done
testcase="org.apache.zookeeper.server.quorum.FuzzySnapshotRelatedTest"
#byteman=""
#for i in `find $HOME/.m2/repository/org/jboss/byteman/byteman/*/**.jar`; do byteman=$i; done

mkdir -p $SCRIPT_DIR/build

java \
-cp $classes_dir:$testclasses_dir:$jars \
-Dbuild.test.dir=$SCRIPT_DIR/build \
org.junit.runner.JUnitCore $testcase
