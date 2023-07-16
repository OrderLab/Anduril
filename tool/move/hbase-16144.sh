#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hbase-16144
hb_dir=$SCRIPT_DIR/../../systems/$case_name
target_dir="$HOME/tmp/bytecode/$case_name/classes"
sootoutput_dir="$HOME/tmp/bytecode/$case_name/sootOutput"

rm -rf $target_dir

cp -r $sootoutput_dir $target_dir
rm -r $target_dir/runtime


rsync -ra $SCRIPT_DIR/../runtime/target/runtime-1.0-jar-with-dependencies.jar $target_dir
