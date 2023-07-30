#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=kafka-10048
ka_dir="${SCRIPT_DIR}/../../systems/$case_name"
t=0
rm -rf $SCRIPT_DIR/deps
mkdir -p $SCRIPT_DIR/deps
for i in `find $ka_dir -name META-INF|grep build`; do
  mkdir -p $SCRIPT_DIR/deps/$t
  cp -r $i $SCRIPT_DIR/deps/$t/
  for j in `ls $i/../*.xml 2>/dev/null`; do
    cp $j $SCRIPT_DIR/deps/$t/
  done
  t=$((t+1))
done
for i in `find $SCRIPT_DIR/deps -name org.apache.kafka.common.config.provider.ConfigProvider`; do
  rm $i
done 
