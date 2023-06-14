#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=cassandra-6415
src_dir=$SCRIPT_DIR/../../systems/$case_name
sootoutput_dir="$HOME/tmp/bytecode/$case_name/sootOutput"

classes_dir="$src_dir/build/classes"
for i in `cd $classes_dir && find . -name "*.class" && cd - >/dev/null 2>&1`; do
  p=$(echo ${i#*org})
  p="org$p"
  if [[ -f "$sootoutput_dir/$p" ]]; then
    rsync -a $sootoutput_dir/$p $classes_dir/$i
  fi
done

rsync $SCRIPT_DIR/../runtime/target/runtime-1.0-jar-with-dependencies.jar $HOME/tmp/bytecode/$case_name/
