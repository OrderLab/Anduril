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
# Suppose you want to install dependency at $DEP
cd $DEP

wget https://builds.openlogic.com/downloadJDK/openlogic-openjdk/8u422-b05/openlogic-openjdk-8u422-b05-linux-x64.tar.gztar xzvf jdk-8u301-linux-x64.tar.gz
tar xzvf openlogic-openjdk-8u422-b05-linux-x64.tar.gz
wget https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz
tar xzvf apache-maven-3.9.9-bin.tar.gz
wget https://dlcdn.apache.org//ant/binaries/apache-ant-1.10.14-bin.tar.gz
tar xzvf apache-ant-1.10.14-bin.tar.gz
export PATH=$PATH:$DEP/openlogic-openjdk-8u422-b05-linux-x64/bin:~/apache-maven-3.9.9/bin:$DEP/apache-ant-1.10.14/bin:$DEP/protobuf-build/bin
export JAVA_HOME=$DEP/openlogic-openjdk-8u422-b05-linux-x64

cp Anduril/systems/protobuf-2.5.0.zip $DEP
cd $DEP/protobuf-2.5.0/
autoreconf -f -i -Wall,no-obsolete
./configure --prefix=$DEP/protobuf-build
make -j4
make install
export PATH=$PATH:$DEP/protobuf-build/bin
protoc --version
```

# 1. Running the experiments
There are 22 cases totaling up. Even though the target system of some of the cases are same (e.g. there are 4 cases in ZooKeeper), the patch version may differ a lot so the compilation, static analysis, and dynamic experiment config differ a lot. To this end, for each unique case, we provides scripts in `evaluation/case_name` to traverse the entire pipeline. `fir-evaluation.sh` is for FIR columns of Table 2 while `fate-evaluation.sh` and `crashtuner=evaluation.sh` are for SOTA solutions. 
## Compile the system codes

## Matchout important logs

## Peforming the static analysis

## Running dynamic experiments

### Evaluate on reproduction





