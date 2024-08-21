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
  ./analyzer-${case_name}.sh
  ../move/${case_name}.sh
  cp ./tree.json $tool_dir/evaluation/$case_name
popd >/dev/null


#echo -e "${R} Recompiling system----------${RESET}"
#pushd $tool_dir/systems/$case_name >/dev/null 
#  compile_after_analysis
#popd >/dev/null

pushd $tool_dir/evaluation/$case_name >/dev/null
  ./update.sh
  # Record fault instances dynamically
  ./run-instrumented-test.sh > record-inject 2>&1  
  # Calculate the time table
  java -jar feedback.jar -tf -g record-inject -b bad-run-log -s tree.json -obj time.bin

  rm -rf results
  mkdir results
  cp fir-trial.sh single-trial.sh

  echo -e "${R} Full Feedback----------${RESET}"
  rm -rf trials/
  cp config-template config.properties
  echo "flakyAgent.feedback=true" >> config.properties
  echo "flakyAgent.augFeedback=true" >> config.properties
  echo "flakyAgent.occurrenceSize=1" >> config.properties
  ./driver.sh 30 > experiment.out 2>&1 
  cp $tool_dir/tool/reporter/target/reporter-1.0-SNAPSHOT-jar-with-dependencies.jar .
  echo -e "${G}Full Feedback result:"
  java -jar reporter-1.0-SNAPSHOT-jar-with-dependencies.jar -t trials/ -s tree.json
  echo -e "${RESET}"
  mkdir results/full_feedback
  cp -a trials/. results/full_feedback/
  cp experiment.out results/full_feedback

  echo -e "${R} Exhaustive Fault Instance----------${RESET}"
  rm -rf trials/
  cp config-template config.properties
  echo "flakyAgent.injectionOccurrenceLimit=1000000" >> config.properties
  echo "flakyAgent.slidingWindow=1000000" >> config.properties
  ./driver.sh 2000 > experiment.out 2>&1 
  cp $tool_dir/tool/reporter/target/reporter-1.0-SNAPSHOT-jar-with-dependencies.jar .
  echo -e "${G}Full Feedback result:"
  java -jar reporter-1.0-SNAPSHOT-jar-with-dependencies.jar -t trials/ -s tree.json
  echo -e "${RESET}"
  mkdir results/exhaustive_fault_instance
  cp -a trials/. results/exhaustive_fault_instance/
  cp experiment.out results/exhaustive_fault_instance/

  echo -e "${R} Fault-site Distance----------${RESET}"
  rm -rf trials/
  cp config-template config.properties
  echo "flakyAgent.injectionOccurrenceLimit=1000000" >> config.properties
  echo "flakyAgent.slidingWindow=10" >> config.properties
  ./driver.sh 300 > experiment.out 2>&1 
  cp $tool_dir/tool/reporter/target/reporter-1.0-SNAPSHOT-jar-with-dependencies.jar .
  echo -e "${G}Full Feedback result:"
  java -jar reporter-1.0-SNAPSHOT-jar-with-dependencies.jar -t trials/ -s tree.json
  echo -e "${RESET}"
  mkdir results/fault_site_distance
  cp -a trials/. results/fault_site_distance/
  cp experiment.out results/fault_site_distance/
  
  echo -e "${R} Fault-site Distance with instance limit----------${RESET}"
  rm -rf trials/
  cp config-template config.properties
  echo "flakyAgent.injectionOccurrenceLimit=3" >> config.properties
  echo "flakyAgent.slidingWindow=10" >> config.properties
  ./driver.sh 50 > experiment.out 2>&1 
  cp $tool_dir/tool/reporter/target/reporter-1.0-SNAPSHOT-jar-with-dependencies.jar .
  echo -e "${G}Full Feedback result:"
  java -jar reporter-1.0-SNAPSHOT-jar-with-dependencies.jar -t trials/ -s tree.json
  echo -e "${RESET}"
  mkdir results/fault_site_distance_w_limit
  cp -a trials/. results/fault_site_distance_w_limit/
  cp experiment.out results/fault_site_distance_w_limit/

  echo -e "${R} Fault-site Feedback----------${RESET}"
  rm -rf trials/
  cp config-template config.properties
  echo "flakyAgent.injectionOccurrenceLimit=3" >> config.properties
  echo "flakyAgent.slidingWindow=10" >> config.properties
  echo "flakyAgent.feedback=true" >> config.properties
  ./driver.sh 30 > experiment.out 2>&1 
  cp $tool_dir/tool/reporter/target/reporter-1.0-SNAPSHOT-jar-with-dependencies.jar .
  echo -e "${G}Full Feedback result:"
  java -jar reporter-1.0-SNAPSHOT-jar-with-dependencies.jar -t trials/ -s tree.json
  echo -e "${RESET}"
  mkdir results/fault_site_feedback
  cp -a trials/. results/fault_site_feedback/
  cp experiment.out results/fault_site_feedback/

  echo -e "${R} Multiply Feedback----------${RESET}"
  rm -rf trials/
  cp config-template config.properties
  echo "flakyAgent.timeFeedback=true" >> config.properties
  ./driver.sh 30 > experiment.out 2>&1 
  cp $tool_dir/tool/reporter/target/reporter-1.0-SNAPSHOT-jar-with-dependencies.jar .
  echo -e "${G}Full Feedback result:"
  java -jar reporter-1.0-SNAPSHOT-jar-with-dependencies.jar -t trials/ -s tree.json
  echo -e "${RESET}"
  mkdir results/multiply_feedback
  cp -a trials/. results/multiply_feedback/
  cp experiment.out results/multiply_feedback/

popd >/dev/null


