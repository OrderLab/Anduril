#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=kafka-12508
ka_dir="${SCRIPT_DIR}/../../systems/$case_name"
classes_dir="$HOME/tmp/bytecode/${case_name}/classes"
runtime_jar="$classes_dir/runtime-1.0-jar-with-dependencies.jar"
#for i in $SCRIPT_DIR/deps/*; do classes_dir="$classes_dir:$i";done
jars="$SCRIPT_DIR"
for i in `find $ka_dir -name "dependant-libs-2.13.5"`; do
  for j in `find $i -name "*.jar"`; do
    if [[ "$j" != *"-SNAPSHOT.jar" ]]; then
      if [[ "$j" != *"/slf4j-log4j12-1.7.30.jar" ]]; then
        jars="$j:$jars";
      fi
    fi
  done
done
for i in `find $ka_dir -name "dependant-libs"`; do
  for j in `find $i -name "*.jar"`; do
    if [[ "$j" != *"-SNAPSHOT.jar" ]]; then
      if [[ "$j" != *"/slf4j-log4j12-1.7.30.jar" ]]; then
        jars="$j:$jars";
      fi
    fi
  done
done

for i in `find $JAVA_HOME -name "*.jar"`; do jars="$i:$jars"; done

#for i in `cat $SCRIPT_DIR/extra-dependencies.txt`; do
#  q=$(echo "$i"|sed "s#/home/tonypan#$HOME#")
#  jars="$q:$jars";
#done

testmethod="org.apache.kafka.streams.integration.EmitOnChangeIntegrationTest#shouldEmitSameRecordAfterFailover"

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
-cp $SCRIPT_DIR/junit-platform-console-standalone-1.7.0.jar:$runtime_jar:$classes_dir:$jars:$SCRIPT_DIR \
-Dbuild.test.dir=$SCRIPT_DIR/build \
runtime.fate.FateAgent $trials_dir $trials_dir/injection-$id.json org.junit.platform.console.ConsoleLauncher --select-method $testmethod \
> $trials_dir/output-$id.txt
sleep 1
id=$(($id + 1))
if [ $id -gt 1000000 ]; then
  break
fi
done
