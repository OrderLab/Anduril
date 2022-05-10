#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

hd_dir="${SCRIPT_DIR}/../../systems/hdfs-12248"
#runtime_classes_dir="$SCRIPT_DIR/runtime-classes"
classes_dir="."
for i in `find $hd_dir/hadoop-common-project/ -name "classes"`; do classes_dir="$i:$classes_dir"; done
for i in `find $hd_dir/hadoop-hdfs-project/ -name "classes"`; do classes_dir="$i:$classes_dir"; done
testclasses_dir="."
for i in `find $hd_dir/hadoop-common-project/ -name "test-classes"`; do testclasses_dir="$i:$testclasses_dir"; done
for i in `find $hd_dir/hadoop-hdfs-project/ -name "test-classes"`; do testclasses_dir="$i:$testclasses_dir"; done
jars="."
for i in `find $hd_dir/hadoop-common-project/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $hd_dir/hadoop-hdfs-project/ -name "*.jar"`; do jars="$i:$jars"; done
for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done
for i in $hd_dir/hadoop-tools/hadoop-distcp/target/lib/*.jar; do jars="$i:$jars"; done
testcase="org.apache.hadoop.hdfs.TestRollingUpgrade"

java \
-Dlog4j.configuration=file:$SCRIPT_DIR/log4j.properties \
-cp $classes_dir:$testclasses_dir:$jars \
org.junit.runner.JUnitCore $testcase
