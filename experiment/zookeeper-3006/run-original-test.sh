#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# Do not use Oracle JVM
if [ $(java -version 2>&1 | grep -ic openjdk) == 0 ]; then
  JAVA_HOME=$HOME/java-8-openjdk-amd64
fi

zk_dir="${SCRIPT_DIR}/../../systems/zookeeper-3006"
#btm_dir="${SCRIPT_DIR}/none"
target="$zk_dir"/zookeeper-server/target/
#runtime_classes_dir="$SCRIPT_DIR/runtime-classes"
classes_dir="$target/classes"
testclasses_dir="$target/test-classes"

jars="."
for i in $target/lib/*.jar; do jars=$i:$jars; done
for i in `find $JAVA_HOME -name "*.jar"`; do jars=$i:$jars; done
testcase="org.apache.zookeeper.test.ZkDatabaseCorruptionTest"

mkdir -p $SCRIPT_DIR/foo/version-2

java \
-cp $classes_dir:$testclasses_dir:$jars \
-Dbuild.test.dir=$SCRIPT_DIR/build \
org.junit.runner.JUnitCore $testcase
