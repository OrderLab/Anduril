#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

#rm -rf *.log

case_name=kafka-10340
ka_dir="${SCRIPT_DIR}/../../systems/$case_name"
#runtime_classes_dir="$SCRIPT_DIR/runtime-classes"
classes_dir="$SCRIPT_DIR"
for i in `find $ka_dir -name "main"`; do
  if [[ "$i" == *"/classes/java/main" ]]; then
    classes_dir="$i:$classes_dir";
  fi
  if [[ "$i" == *"/classes/scala/main" ]]; then
    classes_dir="$i:$classes_dir";
  fi
done

testclasses_dir="$SCRIPT_DIR"

for i in `find $ka_dir -name "test"`; do
  if [[ "$i" == *"/classes/java/test" ]]; then
   testclasses_dir="$i:$testclasses_dir";
  fi
  if [[ "$i" == *"/classes/scala/test" ]]; then
    testclasses_dir="$i:$testclasses_dir";
  fi
done

jars="$SCRIPT_DIR"

for i in `head -n1 $SCRIPT_DIR/extra-depend.txt|tr ',' '\n'`; do
  q=$(echo "$i"|sed 's#file:##')
  if [[ "$i" != *"-SNAPSHOT.jar" ]]; then
    jars="$q:$jars"
  fi
done
#for i in `cat $SCRIPT_DIR/extra-dependencies.txt`; do
#    jars="$i:$jars";
#done

#for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done
testcase="org.apache.kafka.connect.integration.ConnectWorkerIntegrationTest"

java \
-Dlog4j.configuration=file:$SCRIPT_DIR/log4j.properties \
-cp $classes_dir:$testclasses_dir:$jars \
org.junit.runner.JUnitCore $testcase





