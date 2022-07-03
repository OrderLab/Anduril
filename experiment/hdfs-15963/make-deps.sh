#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hdfs-15963
hd_dir="${SCRIPT_DIR}/../../systems/$case_name"

rm -rf $SCRIPT_DIR/deps
mkdir -p $SCRIPT_DIR/deps

move_dep () {
  local dep=$1
  local cls=$2
  mkdir -p $dep
  for f in `find $cls -maxdepth 1 -type f`; do
    rsync -a $f $dep/
  done
}

t=0
for i in `find $hd_dir/hadoop-common-project/ -name "classes"`; do
  move_dep $SCRIPT_DIR/deps/$t $i
  t=$((t+1))
done
for i in `find $hd_dir/hadoop-hdfs-project/ -name "classes"`; do
  move_dep $SCRIPT_DIR/deps/$t $i
  t=$((t+1))
done
for i in `find $hd_dir/hadoop-common-project/ -name "test-classes"`; do
  move_dep $SCRIPT_DIR/deps/$t $i
  t=$((t+1))
done
for i in `find $hd_dir/hadoop-hdfs-project/ -name "test-classes"`; do
  move_dep $SCRIPT_DIR/deps/$t $i
  t=$((t+1))
done
