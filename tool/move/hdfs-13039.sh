#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hdfs-13039
src_dir=$SCRIPT_DIR/../../systems/$case_name
sootoutput_dir="$HOME/tmp/bytecode/$case_name/sootOutput"

for classes_dir in `find $src_dir/hadoop-common-project -name classes`; do
for i in `cd $classes_dir && find . -name "*.class" && cd - >/dev/null 2>&1`; do
  flag=""
  if [[ -z "$flag" ]]; then
    if [[ -f "$sootoutput_dir/$i" ]]; then
      if [[ "$i" != *"org/apache/hadoop/fs/PathHandle.class" ]]; then
        rsync -a $sootoutput_dir/$i $classes_dir/$i
      fi
    fi
  fi
done
done
for classes_dir in `find $src_dir/hadoop-hdfs-project -name classes`; do
for i in `cd $classes_dir && find . -name "*.class" && cd - >/dev/null 2>&1`; do
  flag=""
  if [[ -z "$flag" ]]; then
    if [[ -f "$sootoutput_dir/$i" ]]; then
      if [[ "$i" != *"org/apache/hadoop/hdfs/protocol/HdfsFileStatus.class" ]]; then
        rsync -a $sootoutput_dir/$i $classes_dir/$i
      fi
    fi
  fi
done
done

rsync $SCRIPT_DIR/../runtime/target/runtime-1.0-jar-with-dependencies.jar $HOME/tmp/bytecode/$case_name/
