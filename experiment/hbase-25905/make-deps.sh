#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hbase-25905
hb_dir="${SCRIPT_DIR}/../../systems/$case_name"
t=0
rm -rf $SCRIPT_DIR/deps
mkdir -p $SCRIPT_DIR/deps
for i in `find $hb_dir -name META-INF|grep target`; do
  mkdir -p $SCRIPT_DIR/deps/$t
  cp -r $i $SCRIPT_DIR/deps/$t/
  for j in `ls $i/../*.xml 2>/dev/null`; do
    cp $j $SCRIPT_DIR/deps/$t/
  done
  t=$((t+1))
done
