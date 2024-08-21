#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

logs_dir=$1
total=`find $logs_dir -name "*.out" | wc -l`
for i in `seq 0 $((${total}-1))`
do
  res=`grep -Pzl '(?s)died.*\n.*org.opentest4j.AssertionFailedError: Condition not met within timeout 60000' "$logs_dir/$i.out"`
  if [[ "$res" ]]; then
    echo $res
    break
  fi
done
