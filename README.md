# Anduril

## Overview
Anduril uses static causal analysis and a novel feedback-driven algorithm
to quickly search the enormous fault space for the root-cause
fault and timing.

Table of Contents
=================
* [Requirements](#requirements)
* [0. Install and configure dependencies](#0-install-and-configure-dependencies)

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

# 0. Install and configure dependencies
 
```bash
wget --no-cookies --no-check-certificate --header "Cookie: oraclelicense=accept-securebackup-cookie" https://javadl.oracle.com/webapps/download/GetFile/1.8.0_301-b09/d3c52aa6bfa54d3ca74e617f18309292/linux-i586/jdk-8u301-linux-x64.tar.gz
tar xzvf jdk-8u301-linux-x64.tar.gz
wget https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz
tar xzvf apache-maven-3.9.9-bin.tar.gz
export PATH=$PATH:~/jdk1.8.0_301/bin:~/apache-maven-3.9.9/bin:~/apache-ant-1.10.14/bin
export JAVA_HOME=~/jdk1.8.0_301

```


