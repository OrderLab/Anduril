#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

ca_dir="${SCRIPT_DIR}/../../systems/cassandra-17663"
#runtime_classes_dir="$SCRIPT_DIR/runtime-classes"
classes_dir="$SCRIPT_DIR"
for i in `ls $ca_dir/build/classes`; do classes_dir="$ca_dir/build/classes/$i:$classes_dir"; done
testclasses_dir="$SCRIPT_DIR:$ca_dir/build/test/classes"
jars="$SCRIPT_DIR"
for i in `find $ca_dir/build/lib/jars/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $ca_dir/build/test/lib/jars/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done
testcase="org.apache.cassandra.distributed.test.RepairErrorsTest"

java \
-cp $classes_dir:$testclasses_dir:$jars \
org.junit.runner.JUnitCore $testcase

