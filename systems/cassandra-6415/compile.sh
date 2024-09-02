#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

rm -rf target/
ant jar -Drat.skip=true
# Run until getting test classes downloaded and then kill
nohup ant test -Drat.skip=true > $SCRIPT_DIR/compile-test.out 2>&1 &
pid=$!

while :
do
  if [[ $(grep 'Testsuite' $SCRIPT_DIR/compile-test.out) ]]; then
    break
  fi
  sleep 1
done

kill -9 $pid
sleep 1
kill -9 `jps -l | grep 'JUnitTestRunner' | cut -d " " -f 1`
