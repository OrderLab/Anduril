# Anduril

## Overview
Anduril uses static causal analysis and a novel feedback-driven algorithm
to quickly search the enormous fault space for the root-cause
fault and timing.

Table of Contents
=================
* [Requirements](#requirements)
* [0. Install and configure dependencies](#0-install-and-configure-dependencies)
* [1. Running the experiments](#1-running-the-experiments)

## Requirements

* OS and JDK:
  - Anduril is developed and tested under **Ubuntu 18.04** and **JDK 8**. 
  - Other systems and newer JDKs may also work. We tested a few functionalities on Ubuntu 18.04 but the test is not complete. 

* Hardware: 
  - The basic workflow of Anduril described in this README, which should satisfy the `Artifacts Functional` requirements, can be done in just one single node.

* Git (>= 2.16.2, version control)
* Apache Maven (>= 3.6.3, for Anduril compilation)
* Apache Ant (>= 1.10.9, artifact testing only, for zookeeper compilation)
* JDK8 (openjdk recommended)
* protobuf (==2.5.0, artifact testing only, for HDFS compilation)

# 0. Install and configure dependencies
 
```bash
DEP=$HOME/anduril-dep # modify this path to where you want the dependencies install
cd $DEP

wget https://builds.openlogic.com/downloadJDK/openlogic-openjdk/8u422-b05/openlogic-openjdk-8u422-b05-linux-x64.tar.gztar xzvf jdk-8u301-linux-x64.tar.gz
tar xzvf openlogic-openjdk-8u422-b05-linux-x64.tar.gz
wget https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz
tar xzvf apache-maven-3.9.9-bin.tar.gz
wget https://dlcdn.apache.org//ant/binaries/apache-ant-1.10.14-bin.tar.gz
tar xzvf apache-ant-1.10.14-bin.tar.gz
export PATH=$PATH:$DEP/openlogic-openjdk-8u422-b05-linux-x64/bin:~/apache-maven-3.9.9/bin:$DEP/apache-ant-1.10.14/bin:$DEP/protobuf-build/bin
export JAVA_HOME=$DEP/openlogic-openjdk-8u422-b05-linux-x64

# For artifact evaluation, we provides the zip in Anduril for installing
cp $WHERE_YOU_DOWNLOAD_ANDURIL/Anduril/systems/protobuf-2.5.0.zip $DEP
cd $DEP/protobuf-2.5.0/
autoreconf -f -i -Wall,no-obsolete
./configure --prefix=$DEP/protobuf-build
make -j4
make install
export PATH=$PATH:$DEP/protobuf-build/bin
protoc --version
```

# 1. Running the experiments
There are 22 cases totaling up. Even though the target system of some of the cases are same (e.g. there are 4 cases in ZooKeeper), the patch version may differ a lot so the compilation, static analysis, and dynamic experiment config differ a lot. As to artifact evaluation, for each unique case, we provides scripts in `evaluation/case_name` that go through the entire pipeline. Each script goes through the entire process of compiling system code, finding important logs, performing static analysis, and running dynamic experiments. `fir-evaluation.sh` is for FIR columns of Table 2 while `fate-evaluation.sh` and `crashtuner=evaluation.sh` are for SOTA solutions. 
## Compile the system codes
The first step is to compile the system code into classes so that it can be utilized by our static analysis code. The system codes are in `system/case_name`. There are two goal here: compiling system code and test workload into classes. In some cases, our workload is the integration test or unit test.

In `zookeeper-2247` and `zookeeper-3157`, we need to run `ant test` for some time to fetch the test classes: 
```bash
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
```
In `zookeeper-3006`, `zookeeper-4203`, HDFS, and HBase cases that using Maven: 
```bash
  mvn clean
  mvn install -DskipTests
```

In Kafka cases that using Gradle, we need to run the targe integration test in workload to get its class file. 
```bash
  ./gradlew clean
  ./gradlew jar
  #kafka-12508
  ./gradlew streams:test --tests org.apache.kafka.streams.integration.EmitOnChangeIntegrationTest
  #kafka-9374
  ./gradlew connect:runtime:test --tests org.apache.kafka.connect.integration.BlockingConnectorTest
```
As to artifact evaluation, these are `compile_before_analysis` function in the scripts! 
## Find important logs
In the second step, the goal is to filter out important log entries in the failure log. 

In `experiments/case_name`, there is script that you can run the workload to get the logs. We run two times. 
```bash
  ./run-original-experiment.sh > good-run-log.txt 
  ./run-original-experiment.sh > good-run-log-2.txt 
```
Then, move them to `ground_truth/case_name` together with the failure log named `bad-run-log.txt`. There is script to filter out suspicious log entries. 
```bash
  # Assume there are good-run-log.txt, good-run-log-2.txt, and bad-run-log.txt
  ./make_diff.sh
```
The output are `diff_log_original.txt`, `diff_log_dd.txt`, and `diff_log_dd_set.txt` in the directory `ground_truth/case_name`. Take an example of the format:
```bash
# First is the class and second is the line number
LeaderRequestProcessor 77
MBeanRegistry 128
ZooKeeperCriticalThread 48
PrepRequestProcessor 965
ClientCnxn$SendThread 1181
AppenderDynamicMBean 209
...
```
## Peform static analysis
The scripts are in directory `tool/bin`, for case `case_name`, 
## Run dynamic experiments

### Evaluate on reproduction





