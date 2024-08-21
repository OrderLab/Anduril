#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hdfs-12070

tool_dir="${SCRIPT_DIR}/../.."
R='\033[0;31m'
G='\033[0;32m'
RESET='\033[0m'

function compile_before_analysis() {
  mvn clean
  mvn install -DskipTests
}

function compile_after_analysis() {
  mvn install -DskipTests
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
  crashtuner= ./analyzer-${case_name}.sh
  ../move/${case_name}.sh
  cp ./tree.json $tool_dir/evaluation/$case_name
popd >/dev/null


#echo -e "${R} Recompiling system----------${RESET}"
#pushd $tool_dir/systems/$case_name >/dev/null 
#  compile_after_analysis
#popd >/dev/null

pushd $tool_dir/evaluation/$case_name >/dev/null
  ./update.sh

  rm -rf crashtuner-results
  mkdir crashtuner-results
  cp crashtuner-trial.sh single-trial.sh
  cp config-sota config.properties

  echo -e "${R} Crashtuner----------${RESET}"
  rm -rf trials/
  ./driver-sota.sh 2000  > experiment.out 2>&1 
  cp $tool_dir/tool/reporter/target/reporter-1.0-SNAPSHOT-jar-with-dependencies.jar .
  echo -e "${G}Fate result:"
  ./check-${case_name}.sh trials
  echo -e "${RESET}"
  cp -a trials/. crashtuner-results/
  cp experiment.out crashtuner-results/

popd >/dev/null


