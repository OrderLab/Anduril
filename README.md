# Anduril

## Overview
Anduril (old codename: FIR) uses static causal analysis and a novel feedback-driven
algorithm to quickly search the enormous fault space for the root-cause fault
and timing.

Table of Contents
=================
* [Requirements](#requirements)
* [0. Install and configure dependencies](#0-install-and-configure-dependencies)
* [1. Clone the repository](#1-clone-the-repository)
* [2. Run the main experiments](#2-run-the-main-experiments)
   * [2.1 Compile the system codes](#21-compile-the-system-codes)
   * [2.2 Find important logs](#22-find-important-logs)
   * [2.3 Peform static analysis](#23-peform-static-analysis)
   * [2.4 Run dynamic experiments](#24-run-dynamic-experiments)
      * [2.4.1 Preparation of the experiment](#241-preparation-of-the-experiment)
      * [2.4.2 Config of the experiment](#242-config-of-the-experiment)
         * [(Example from Artifact evaluation) FIR columns in Table II](#example-from-artifact-evaluation-fir-columns-in-table-ii)
         * [(Example from Artifact evaluation) FIR columns in Table II](#example-from-artifact-evaluation-fir-columns-in-table-ii-1)
      * [2.4.3 (Optional) Prepare time table](#243-optional-prepare-time-table)
      * [2.4.4 Run the experiment](#244-run-the-experiment)
      * [2.4.5 Check the reproduction result](#245-check-the-reproduction-result)
* [3. Artifact evaluation](#3-artifact-evaluation)
   * [Table II](#table-ii)
      * [Edit the scripts](#edit-the-scripts)
      * [Run the script](#run-the-script)
      * [Inspect the result](#inspect-the-result)
   * [Table III](#table-iii)

## Requirements

* OS and JDK:
  - Anduril is developed and tested under **Ubuntu 18.04 to 20.04** with **JDK 8**. 
  - Other systems and newer JDKs may also work.

* Hardware: 
  - The basic workflow of Anduril described in this README can be done in just one single node.
  - Our experiment node uses the CloudLab `c220g5` node type, which has two
    Intel Xeon Silver 4114 10-core CPUs at 2.20 GHz, 192GB ECC DDR4-2666 memory, 
    and a 1 TB 7200 RPM 6G SAS HDs.

* Git (>= 2.16.2, version control)
* Apache Maven (>= 3.6.3, for Anduril compilation)
* Apache Ant (>= 1.10.9, artifact testing only, for zookeeper compilation)
* JDK8 (openjdk recommended)
* protobuf (==2.5.0, artifact testing only, for HDFS compilation)

# 0. Install and configure dependencies
 
```bash
sudo apt-get update
sudo apt install git maven ant vim openjdk-8-jdk
sudo update-alternatives --set java $(sudo update-alternatives --list java | grep "java-8")

export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
echo export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 >> ~/.bashrc
```

If you do not have root permissions, install the dependencies this way:

<details>
<summary>Rootless installation</summary>

```bash
DEP=$HOME/anduril-dep # modify this path to where you want the dependencies installed
cd $DEP

wget https://builds.openlogic.com/downloadJDK/openlogic-openjdk/8u422-b05/openlogic-openjdk-8u422-b05-linux-x64.tar.gztar xzvf jdk-8u301-linux-x64.tar.gz
tar xzvf openlogic-openjdk-8u422-b05-linux-x64.tar.gz
wget https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz
tar xzvf apache-maven-3.9.9-bin.tar.gz
wget https://dlcdn.apache.org//ant/binaries/apache-ant-1.10.14-bin.tar.gz
tar xzvf apache-ant-1.10.14-bin.tar.gz

export PATH=$PATH:$DEP/openlogic-openjdk-8u422-b05-linux-x64/bin:~/apache-maven-3.9.9/bin:$DEP/apache-ant-1.10.14/bin:$DEP/protobuf-build/bin
export JAVA_HOME=$DEP/openlogic-openjdk-8u422-b05-linux-x64

echo "export PATH=$DEP/openlogic-openjdk-8u422-b05-linux-x64/bin:~/apache-maven-3.9.9/bin:$DEP/apache-ant-1.10.14/bin:$DEP/protobuf-build/bin:\$PATH" >> ~/.bashrc
echo "export JAVA_HOME=$DEP/openlogic-openjdk-8u422-b05-linux-x64" >> ~/.bashrc
```

</details>

Install protobuf, which is needed for HDFS compilation:

```bash
DEP=$HOME/anduril-dep # modify this path to where you want the dependencies installed
cd $DEP
wget https://github.com/OrderLab/Anduril/blob/main/systems/protobuf-2.5.0.zip
unzip protobuf-2.5.0.zip
cd protobuf-2.5.0/
autoreconf -f -i -Wall,no-obsolete
./configure --prefix=$DEP/protobuf-build
make -j4
make install
export PATH=$DEP/protobuf-build/bin:$PATH
echo "export PATH=$DEP/protobuf-build/bin:\$PATH" >> ~/.bashrc
protoc --version
```

# 1. Clone the repository

```bash
git clone https://github.com/OrderLab/Anduril.git
```

This repository contains the evaluated systems, so it is a bit large (around 3.5 GB). Make sure you have enough disk space.


# 2. Run the main experiments

There are 22 cases totaling up. Even though the target system of some of the
cases are same (e.g. there are 4 cases in ZooKeeper), the patch version may
differ a lot so the compilation, static analysis, and dynamic experiment config
differ a lot. 

## 2.1 Compile the system codes

The first step is to compile the system code into classes so that they can be
utilized by our static analyzer.  The system codes are in the directory
`system/case_name`. We need to switch to that directory and then run the
compilation commands. Besides the system code, we may also need to compile the
tests in the system code directory, which will serve as the workload for that
case.

Since the compilations commands differ by cases, we prepare a `compile.sh` script
in each case directory that you can invoke. For example:

```bash
cd systems/zookeeper-3006
./compile.sh
```

We also provide a script to compile all cases:

```bash
cd systems
./compile-all.sh
```

## 2.2 Find important logs

In the second step, the goal is to filter out important log entries in the failure log. 

In `experiments/case_name`, there is a script that you can run the workload to get the logs. We run two times. 

```bash
  ./run-original-experiment.sh > good-run-log.txt 
  ./run-original-experiment.sh > good-run-log-2.txt 
```

Then, move them to `ground_truth/case_name` together with the failure log named `bad-run-log.txt`. There is a script to filter out suspicious log entries. 

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

## 2.3 Peform static analysis

The scripts are in directory `tool/bin`. For case `case_name`, `analyzer-${case_name}.sh` will output causal graph `tree.json` in the directory you run the script and the instrumented class files. There is another post-processing step on the generated instrumnted class files through scripts in `tool/move`. 

```bash
  tool/bin/analyze-${case_name}.sh
  tool/move/${case_name}.sh
```

For the state-of-the-art baselines, 

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

## 2.4 Run dynamic experiments

### 2.4.1 Preparation of the experiment
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

### 2.4.2 Config of the experiment
The configuration file is `config.properties`. 

#### (Example from Artifact evaluation) FIR columns in Table II 

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

#### (Example from Artifact evaluation) FIR columns in Table II 

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

### 2.4.3 (Optional) Prepare time table
If your configuration contains `flaky.timeFeedback=true` pr `flaky.augFeedback=true`, time table is needed. 
```bash
  ./make-depps.sh # If it is in evaluation/case_name
  ./run-instrumnted-experiment.sh > record-inject
  java -jar reporter-1.0-SNAPSHOT-jar-with-dependencies.jar -t trials/ -s tree.json
```
### 2.4.4 Run the experiment

Driver will run the experiments and output the trials into `trials`. For trial with index i, `injection-$i.json` records the fault injection point while `$i.out` records the system output. 
FIR: 
```bash
  ./driver.sh num_trials
```
SOTA:
```bash
  ./driver-sota.sh num_trials
```

### 2.4.5 Check the reproduction result

There are two options, if `check-${case_name}.sh` is in the evaluation dir, we should use 
```bash
 `check-${case_name}.sh` trials 
```
Else, it is incoporated into our reporter framework and can be checked with
```bash
  java -jar reporter-1.0-SNAPSHOT-jar-with-dependencies.jar -t trials/ -s tree.json
```

We will uniformize it soon!

# 3. Artifact evaluation 

The scripts are stored in `evaluation/scripts`. 

## Table II

We need three scripts `fir-evaluation.sh`, `fate-evaluation.sh` and `crashtuner-evaluation.sh`. `fir-evaluation.sh` is for the first 6 columns while `fate-evaluation.sh` and `crashtuner-evaluation.sh` are for SOTA. 

Suppose you want to get the row of `case_name`, copy the three scripts into the folder `evaluaiton/case_name`

The three scripts can be ran on three different machines. Before running the script, there are some fields needed to be edited"

### Edit the scripts 

In `fir-evaluation.sh`, the case_name should be changed to `case_name`. `fir-evaluation.sh` will run the 6 experiments shown in Table II sequentially and `p1-p6` designate how many trials each experiment lasts. For example, if you set `p1` to `20`, the first experiment, `Full Feedback`, would last `20` trials. A rule of thumb is to set this to be two times the data in the Table II. It it exceeds `2000`, decrease it to `2000`. Or it can not be finished in one day. Last but not the least, `compile_before_analysis` should also be edited to reflect the system of the case. You can refer to section 1 about it. By default, it works for all system projects using Maven.  

```bach
#!/usr/bin/env bash
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=zookeeper-2247

p1=1
p2=1
p3=1
p4=1
p5=1
p6=1

tool_dir="${SCRIPT_DIR}/../.."
R='\033[0;31m'
G='\033[0;32m'
RESET='\033[0m'

function compile_before_analysis() {
  mvn clean
  mvn install -DskipTests
}
```
As to `fate-evaluation.sh` or `crashtuner-evaluation.sh`, there is only one experiment, so only `p1` exists. 
```bash
#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=zookeeper-2247
p1=1

tool_dir="${SCRIPT_DIR}/../.."
R='\033[0;31m'
G='\033[0;32m'
RESET='\033[0m'

function compile_before_analysis() {
  mvn clean
  mvn install -DskipTests
}
```
Also note that for some cases, the three scripts are already there. You can directly run them and they serve as good examples for you do other experiments. 
### Run the script
They traverses the entire pipeline in section I, so you can just run the script to get the results. 
```bash
./fir-evaluation.sh
```
```bash
./fate-evaluation.sh
```
```bash
./crashtuner-evaluation.sh
```
### Inspect the result
The first index of the trial in which the case is reproduced will be printed in `Green` color. 
```bash
  echo -e "${G}Full Feedback result:"
  ./check-${case_name}.sh trials
  echo -e "${RESET}"
```
## Table III
Same idea as Table I. Edit and run `parameter-evaluation.sh` in `evaluaiton/artifact-evaluation`.

