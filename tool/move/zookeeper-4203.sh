#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=zookeeper-4203
classes_dir="$SCRIPT_DIR/../../systems/$case_name/zookeeper-server/target/classes"
test_classes_dir="$SCRIPT_DIR/../../systems/$case_name/zookeeper-server/target/test-classes"
target_dir="$HOME/tmp/bytecode/$case_name/classes"
sootoutput_dir="$HOME/tmp/bytecode/$case_name/sootOutput"

rm -rf $target_dir
mkdir -p $target_dir

defaultList="./org/apache/zookeeper/server/auth/AuthenticationProvider.class
./org/apache/zookeeper/server/watch/IWatchManager.class
./org/apache/zookeeper/metrics/Counter.class
./org/apache/zookeeper/audit/AuditLogger.class"

for i in `cd $classes_dir && find . -name "*.class" && cd - >/dev/null 2>&1`; do
  flag=""
  for j in $defaultList; do
    if [[ "$i" == "$j" ]]; then
      flag="1"
    fi
  done
  mkdir -p $(dirname $target_dir/$i)
  if [[ -z "$flag" ]]; then
    rsync -a $sootoutput_dir/$i $target_dir/$i
  else
    rsync -a $classes_dir/$i $target_dir/$i
  fi
done
for i in `cd $test_classes_dir && find . -name "*.class" && cd - >/dev/null 2>&1`; do
  flag=""
  for j in $defaultList; do
    if [[ "$i" == "$j" ]]; then
      flag="1"
    fi
  done
  mkdir -p $(dirname $target_dir/$i)
  if [[ -z "$flag" ]]; then
    rsync -a $sootoutput_dir/$i $target_dir/$i
  else
    rsync -a $test_classes_dir/$i $target_dir/$i
  fi
done

rsync -ra $SCRIPT_DIR/../runtime/target/classes/runtime $target_dir
