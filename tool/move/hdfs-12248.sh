#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hdfs-12248
src_dir=$SCRIPT_DIR/../../systems/$case_name
target_dir="$HOME/tmp/bytecode/$case_name/classes"
sootoutput_dir="$HOME/tmp/bytecode/$case_name/sootOutput"

rm -rf $target_dir
mkdir -p $target_dir

cp -a $sootoutput_dir/. $target_dir


for classes_dir in `find $src_dir/hadoop-common-project -name classes`; do
for i in `cd $classes_dir && find . -name "*.class" && cd - >/dev/null 2>&1`; do
  flag=""
  mkdir -p $(dirname $target_dir/$i)
  if [[ -z "$flag" ]]; then
    if [[ -f "$sootoutput_dir/$i" ]]; then
      rsync -a $sootoutput_dir/$i $target_dir/$i
    fi
  else
    if [[ -f "$classes_dir/$i" ]]; then
      rsync -a $classes_dir/$i $target_dir/$i
    fi
  fi
done
done
for classes_dir in `find $src_dir/hadoop-hdfs-project -name classes`; do
for i in `cd $classes_dir && find . -name "*.class" && cd - >/dev/null 2>&1`; do
  flag=""
  mkdir -p $(dirname $target_dir/$i)
  if [[ -z "$flag" ]]; then
    if [[ -f "$sootoutput_dir/$i" ]]; then
      rsync -a $sootoutput_dir/$i $target_dir/$i
    fi
  else
    if [[ -f "$classes_dir/$i" ]]; then
      rsync -a $classes_dir/$i $target_dir/$i
    fi
  fi
done
done

for test_classes_dir in `find $src_dir/hadoop-common-project -name test-classes`; do
for i in `cd $test_classes_dir && find . -name "*.class" && cd - >/dev/null 2>&1`; do
  flag=""
  mkdir -p $(dirname $target_dir/$i)
  if [[ -z "$flag" ]]; then
    if [[ -f "$sootoutput_dir/$i" ]]; then
      rsync -a $sootoutput_dir/$i $target_dir/$i
    fi
  else
    if [[ -f "$test_classes_dir/$i" ]]; then
      rsync -a $test_classes_dir/$i $target_dir/$i
    fi
  fi
done
done
for test_classes_dir in `find $src_dir/hadoop-hdfs-project -name test-classes`; do
for i in `cd $test_classes_dir && find . -name "*.class" && cd - >/dev/null 2>&1`; do
  flag=""
  mkdir -p $(dirname $target_dir/$i)
  if [[ -z "$flag" ]]; then
    if [[ -f "$sootoutput_dir/$i" ]]; then
      rsync -a $sootoutput_dir/$i $target_dir/$i
    fi
  else
    if [[ -f "$test_classes_dir/$i" ]]; then
      rsync -a $test_classes_dir/$i $target_dir/$i
    fi
  fi
done
done

rsync -ra $SCRIPT_DIR/../runtime/target/runtime-1.0-jar-with-dependencies.jar $target_dir
