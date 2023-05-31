#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=cassandra-17663
ca_dir="${SCRIPT_DIR}/../../systems/$case_name"

rm -rf $SCRIPT_DIR/test
mkdir -p $SCRIPT_DIR/test
cp -a $ca_dir/test/conf $SCRIPT_DIR/test
t=0
rm -rf $SCRIPT_DIR/deps
mkdir -p $SCRIPT_DIR/deps
#for i in `find $ca_dir -name META-INF`; do
#  mkdir -p $SCRIPT_DIR/deps/$t
#  cp -r $i $SCRIPT_DIR/deps/$t/
#  t=$((t+1))
#done

for i in `find $ca_dir -name resources`; do
  mkdir -p $SCRIPT_DIR/deps/$t
  cp -a $i/. $SCRIPT_DIR/deps/$t/
  t=$((t+1))
done

cp $SCRIPT_DIR/logback-dtest.xml $SCRIPT_DIR/test/conf
#mkdir -p $SCRIPT_DIR/deps/$t
#cp -a $ca_dir/build/test/classes/. $SCRIPT_DIR/deps/$t/ 
