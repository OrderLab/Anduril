#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=zookeeper-2247

tool_dir="${SCRIPT_DIR}/../.."
R='\033[0;31m'
G='\033[0;32m'
RESET='\033[0m'

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
echo -e "${R} Compiling system----------${RESET} \n"
pushd $tool_dir/systems/$case_name >/dev/null 

compile_before_analysis

popd >/dev/null

# Filter out important log messages
echo -e "${R} Log diff----------${RESET}"
pushd $tool_dir/ground_truth/$case_name >/dev/null 
  ./make_diff.sh $tool_dir/tool/feedback/target/feedback-1.0-jar-with-dependencies.jar
popd >/dev/null

# Perform static analysis
echo -e "${R} Static analysis----------${RESET}"
pushd $tool_dir/tool/bin >/dev/null 
  fate= ./analyzer-${case_name}.sh
  ../move/${case_name}.sh
  cp ./tree.json $tool_dir/evaluation/$case_name
popd >/dev/null


#echo -e "${R} Recompiling system----------${RESET}"
#pushd $tool_dir/systems/$case_name >/dev/null 
#  compile_after_analysis
#popd >/dev/null

pushd $tool_dir/evaluation/$case_name >/dev/null
  ./update.sh

  rm -rf fate-results
  mkdir fate-results
  cp fate-trial.sh single-trial.sh

  echo -e "${R} Fate----------${RESET}"
  rm -rf trials/
  ./driver.sh 2000 > experiment.out 2>&1 
  cp $tool_dir/tool/reporter/target/reporter-1.0-SNAPSHOT-jar-with-dependencies.jar .
  echo -e "${G}Fate result:"
  java -jar reporter-1.0-SNAPSHOT-jar-with-dependencies.jar -t trials/ -b -n $case_name
  echo -e "${RESET}"
  cp -a trials/. fate-results/
  cp experiment.out fate-results/

popd >/dev/null


