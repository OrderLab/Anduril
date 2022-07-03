#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hbase-15252
hb_dir="${SCRIPT_DIR}/../../systems/$case_name"
#runtime_classes_dir="$SCRIPT_DIR/runtime-classes"
classes_dir="$SCRIPT_DIR"
for i in `find $hb_dir -name "classes"`; do classes_dir="$i:$classes_dir"; done
testclasses_dir="$SCRIPT_DIR"
for i in `find $hb_dir -name "test-classes"`; do testclasses_dir="$i:$testclasses_dir"; done
jars="$SCRIPT_DIR"
for i in `head -n1 $hb_dir/target/cached_classpath.txt|tr ':' '\n'`; do
  if [[ "$i" == *"/.m2/repository/"* ]]; then
    jars="$i:$jars"
  fi
done

for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done

testcase="org.apache.hadoop.hbase.regionserver.wal.TestWALReplay"

java \
-Dlog4j.configuration=file:$SCRIPT_DIR/log4j.properties \
-cp $classes_dir:$testclasses_dir:$jars \
org.junit.runner.JUnitCore $testcase
