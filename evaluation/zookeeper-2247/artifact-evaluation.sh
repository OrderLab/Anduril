#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=zookeeper-2247

tool_dir="${SCRIPT_DIR}/../.."


# Compile System code
pushd $tool_dir/systems/$case_name >/dev/null 

ant clean
ant jar 
# Run until getting test classes downloaded and then kill
nohup ant test > $SCRIPT_DIR/compile-test.out 2>&1 &
pid=$!

while : 
do
  if [[ $(grep 'junit.run-concurrent:' $SCRIPT_DIR/compile-test.out) ]]; then
    break
  fi
  sleep 1
done

kill -9 $pid
sleep 1
kill -9 `jps -l | grep 'JUnitTestRunner' | cut -d " " -f 1`

popd >/dev/null

pushd $SCRIPT_DIR >/dev/null

#java -jar $SCRIPT_DIR/driver.jar \
#--experiment \
#--spec $SCRIPT_DIR/tree.json \
#--path $SCRIPT_DIR/trials \
#--config $SCRIPT_DIR/config.properties \
#--trial-limit $1 \
#$@

popd >/dev/null

