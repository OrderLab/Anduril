#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

zk_dir="$1"
#btm_dir="${SCRIPT_DIR}/none"
#target="$zk_dir"/zookeeper-server/target/
case_name=zookeeper-3157
classes_dir="$HOME/tmp/bytecode/$case_name/classes"
#runtime_classes_dir="$HOME/tmp/bytecode/$case_name/runtime-classes"
#testclasses_dir="$target/test-classes"

jars="."
for i in $zk_dir/build/lib/*.jar; do jars="$i:$jars"; done
for i in $zk_dir/build/test/lib/*.jar; do jars="$i:$jars"; done
for i in `find $HOME/.m2 -name "*.jar"|grep 'javax.json'|grep '1.1.4'`; do jars="$i:$jars"; done
#for i in `find $HOME/.m2/repository/org/jboss/byteman/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done
testcase="org.apache.zookeeper.server.quorum.FuzzySnapshotRelatedTest"
#byteman=""
#for i in `find $HOME/.m2/repository/org/jboss/byteman/byteman/*/**.jar`; do byteman=$i; done

mkdir -p $SCRIPT_DIR/build

java \
-cp $classes_dir:$jars \
-Dbuild.test.dir=$SCRIPT_DIR/build \
-DflakyAgent.fixPointInjectionMode=true \
-DflakyAgent.injectionId=$2 \
-DflakyAgent.injectionTimes=$3 \
-DflakyAgent.fault=$4 \
-DflakyAgent.traceFile=$5 \
org.junit.runner.JUnitCore $testcase
