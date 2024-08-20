#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=zookeeper-2247

tool_dir="${SCRIPT_DIR}/../.."


function compile_before_analysis() {
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
}

# Compile System code
echo "Compiling system----------"
pushd $tool_dir/systems/$case_name >/dev/null 

compile_before_analysis

popd >/dev/null

# Filter out important log messages
echo "Log diff----------"
pushd $tool_dir/ground_truth/$case_name >/dev/null 
  ./make_diff.sh $tool_dir/tool/feedback/target/feedback-1.0-jar-with-dependencies.jar
popd >/dev/null

# Perform static analysis
echo "Static analysis----------"
pushd $tool_dir/tool/bin >/dev/null 
  ./analyzer-${case_name}.sh
  ../move/${case_name}.sh
  cp ./tree.json $SCRIPT_DIR
popd >/dev/null

# Record fault instances dynamically


pushd $SCRIPT_DIR >/dev/null

#java -jar $SCRIPT_DIR/driver.jar \
#--experiment \
#--spec $SCRIPT_DIR/tree.json \
#--path $SCRIPT_DIR/trials \
#--config $SCRIPT_DIR/config.properties \
#--trial-limit $1 \
#$@

popd >/dev/null

