#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

logs_dir=$1
total=`find $logs_dir -name "*.out" | wc -l`
for i in `seq 0 $((${total}-1))`
do
  res=`grep -Pzl '(?s)There was 1 failure:.*\n.*testZKLockCleaner\(TestMultiSlaveReplication.java:244\)' "$logs_dir/$i.out"`
  if [[ "$res" ]]; then
    echo $res
    break
  fi
done
