#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hbase-20492
hb_dir=$SCRIPT_DIR/../../systems/$case_name
target_dir="$HOME/tmp/bytecode/$case_name/classes"
sootoutput_dir="$HOME/tmp/bytecode/$case_name/sootOutput"

rm -rf $target_dir

cp -r $sootoutput_dir $target_dir

for i in `cat $SCRIPT_DIR/default-$case_name.txt`; do
  q=$(echo "$i"|sed 's#/src/main/java/#/target/classes/#')
  p=$(echo "$i"|sed 's#^./.*/src/main/java/##')
  rsync -a $hb_dir/$q $target_dir/$p
done

rsync -ra $SCRIPT_DIR/../runtime/target/runtime-1.0-jar-with-dependencies.jar $target_dir
