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
There are 22 cases totaling up. Even though the target system of some of the cases are same (e.g. there are 4 cases in ZooKeeper), the patch version may differ a lot so the compilation, static analysis, and dynamic experiment config differ a lot. 
## Compile the system codes
The first step is to compile the system code into classes so that it can be utilized by our static analysis code. The system codes are in `system/case_name`. There are two goal here: compiling system code and test workload into classes. In some cases, our workload is the integration test or unit test.

In `zookeeper-2247`, `zookeeper-3157` and Cassandra cases, we need to run `ant test` for some time to fetch the test classes: 
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
  #kafka-10048
  ./gradlew connect:mirror:test --tests org.apache.kafka.connect.mirror.MirrorConnectorsIntegrationTest
```
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
For artifact evaluation, we do not have this stage because the results are already achived and there is no need to rerun them. 
## Peform static analysis
The scripts are in directory `tool/bin`. For case `case_name`, `analyzer-${case_name}.sh` will output causal graph `tree.json` in the directory you run the script and the instrumented class files. There is another post-processing step on the generated instrumnted class files through scripts in `tool/move`. 
```bash
  tool/bin/analyze-${case_name}.sh
  tool/move/${case_name}.sh
```
For SOTA, 

Static analysis of Fate
```bash
  fate= tool/bin/analyze-${case_name}.sh
  tool/move/${case_name}.sh
```
Static analysis of Crashtuner
```bash
  crashtuner= tool/bin/analyze-${case_name}.sh
  tool/move/${case_name}.sh
```
For artifact evaluation, the scripts do this and move `tree.json` to `evaluation/case_name` for later dynamic experiments. 
## Run dynamic experiments
### Preparation of the experiment
All the evaluation should happen in `evaluation/case_name` directory. 
For 
```bash
  cd evaluation/case_name
  cp $DIR_WHERE_YOU_PERFORM_STATIC_ANALYSIS/tree.json .
  ./update.sh
```
If it is FIR:
```bash
  cp fir-trial.sh single-trial.sh
```
Fate: 
```bash
  cp fate-trial.sh single-trial.sh
```
Crashtuner: 
```bash
  cp crashtuner-trial.sh single-trial.sh
```
### Config of the experiment
The configuration file is `config.properties`. 

#### (Artifact evaluation) FIR columns in Table II 
There is one extra file called `config-template`. We can make the 6 corresponding `config.properties` from it by attaching extra configuration. 
For example, in `zookeeper-2247`, `config-template`
```bash
  flakyAgent.avoidBlockMode=true
  flakyAgent.probability=0.05
  flakyAgent.timePriorityTable=time.bin
  flakyAgent.timeFeedbackMode=min_times
  flakyAgent.trialTimeout=90
  flakyAgent.recordOnthefly=true
```
The `config.properties` for Full Feedback can be generated through: 
```bash
  cp config-template config.properties
  echo "flakyAgent.feedback=true" >> config.properties
  echo "flakyAgent.augFeedback=true" >> config.properties
  echo "flakyAgent.occurrenceSize=1" >> config.properties
```
You can refer to `fir-evaluation.sh` for all the 6 policies in FIR

#### (Artifact evaluation) FIR columns in Table II 
There is one extra file called `config-sota`:
```bash
flakyAgent.trialTimeout=90
flakyAgent.recordOnthefly=true
```
The `config.properties` for either Fate or Crashtuner can be generated through: 
```bash
  cp config-sota config.properties
```
You can refer to `fate-evaluation.sh` or `crashtuner-evaluation.sh` to see what happens.

### (Optional) Prepare time table
If your configuration contains `flaky.timeFeedback=true` pr `flaky.augFeedback=true`, time table is needed. 
```bash
  ./make-depps.sh # If it is in evaluation/case_name
  ./run-instrumnted-experiment.sh > record-inject
  java -jar reporter-1.0-SNAPSHOT-jar-with-dependencies.jar -t trials/ -s tree.json
```
### Running the experiment
Driver will run the experiments and output the trials into `trials`. For trial with index i, `injection-$i.json` records the fault injection point while `$i.out` records the system output. 
FIR: 
```bash
  ./driver.sh num_trials
```
SOTA:
```bash
  ./driver-sota.sh num_trials
```
#### (Artifact evaluation) Table II 
In `fir-evaluation.sh`, `fate-evaluation.sh`, and `crashtuner-evaluation.sh`, the user should edit num_trials to match with the data in Table II. A rule of thumb is to set num_trials to be two times the data in the table. It it exceeds `2000`, decrease it to `2000`. Or it can not be finished in one day. 

### Check whether reproduction
There are two options, if `check-${case_name}.sh` is in the evaluation dir, we should use 
```bash
 `check-${case_name}.sh` trials 
```
Else, it is incoporated into our reporter framework and can be checked with
```bash
  java -jar reporter-1.0-SNAPSHOT-jar-with-dependencies.jar -t trials/ -s tree.json
```

In artifact evaluation, after each policy, it will check the reproduction and the result is in green color. 

# 2. Artifact evaluation 
## Table II
As to artifact evaluation, for each unique case, we provides scripts in `evaluation/case_name` that go through the entire pipeline. Each script goes through the entire process of compiling system code, finding important logs, performing static analysis, and running dynamic experiments. `fir-evaluation.sh` is for FIR columns of Table 2 while `fate-evaluation.sh` and `crashtuner=evaluation.sh` are for SOTA solutions. 

