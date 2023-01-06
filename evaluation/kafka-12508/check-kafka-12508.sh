#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

classes_dir="$SCRIPT_DIR/trials"
for i in `find $classes_dir -name "*.out"`; do
  grep -Pzl '(?s)died.*\n.*org.opentest4j.AssertionFailedError: Condition not met within timeout 60000' $i
done
