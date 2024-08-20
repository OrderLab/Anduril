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

function compile_after_analysis() {
  ant jar
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
  cp ./tree.json $tool_dir/evaluation/$case_name
popd >/dev/null


echo "Recompiling system----------"
pushd $tool_dir/systems/$case_name >/dev/null 
  compile_after_analysis
popd >/dev/null

pushd $tool_dir/evaluation/$case_name >/dev/null
  ./update.sh
  # Record fault instances dynamically
  ./run-instrumented-test.sh > record-inject 2>&1  
  # Calculate the time table
  java -jar feedback.jar -tf -g record-inject -b bad-run-log -s tree.json -obj time.bin


  # Perform evaluation
  ./driver.sh 10
  cp $tool_dir/tool/reporter/target/reporter-1.0-SNAPSHOT-jar-with-dependencies.jar .
  java -jar reporter-1.0-SNAPSHOT-jar-with-dependencies.jar -t trials/ -s tree.json
popd >/dev/null


