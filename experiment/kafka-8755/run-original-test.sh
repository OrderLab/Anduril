#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=kafka-8755
ka_dir="${SCRIPT_DIR}/../../systems/$case_name"
gr_dir="$HOME/.gradle/caches"
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
for i in `find $ka_dir -name "dependant-libs-2.12.9"`; do
  for j in `find $i -name "*.jar"`; do
    if [[ "$j" != *"/dependant-libs-2.12.9/kafka-"*".jar" ]]; then
      if [[ "$j" != *"/slf4j-log4j12-"*".jar" ]]; then
        jars="$j:$jars";
      fi
    fi
  done
done 
for i in `find $ka_dir -name "dependant-libs"`; do
  for j in `find $i -name "*.jar"`; do
    if [[ "$j" != *"/dependant-libs/kafka-"*".jar" ]]; then 
      if [[ "$j" != *"/slf4j-log4j12-"*".jar" ]]; then
        jars="$j:$jars";
      fi 
    fi
  done
done
#Cached Dependencies
#for i in `find ~/.gradle/caches/modules-2/files-2.1/ -name "*.jar"`; do
#    jars="$i:$jars";
#done
for i in `cat $SCRIPT_DIR/extra-dependencies.txt`; do
  for j in `find $gr_dir -name $i`; do
    #echo $j
    jars="$j:$jars";
    break
  done 
done

for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done


testmethod="org.apache.kafka.streams.integration.StandbyTaskTest#shouldTestStandbyTask"

java \
-jar $SCRIPT_DIR/junit-platform-console-standalone-1.7.0.jar \
-cp "$ka_dir/core/build/dependant-libs-2.12.9/slf4j-log4j12-1.7.27.jar":$classes_dir:$testclasses_dir:$jars \
--select-method $testmethod
