#!/bin/bash

./gradlew clean
./gradlew jar
./gradlew connect:runtime:test --tests org.apache.kafka.connect.integration.BlockingConnectorTest
