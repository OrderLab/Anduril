#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

logs_dir=$1
total=`find $logs_dir -name "*.out" | wc -l`
for i in `seq 0 $((${total}-1))`
do
  res=`grep -Pzl '(?s)Initialization failed for Block pool.*\n.*java.io.IOException: Timed out waiting for Mini HDFS Cluster to start' "$logs_dir/$i.out"`
  if [[ "$res" ]]; then
    echo $res
    break
  fi
done
