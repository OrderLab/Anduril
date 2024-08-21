#!/bin/bash

./gradlew clean
./gradlew jar
./gradlew connect:mirror:test --tests org.apache.kafka.connect.mirror.MirrorConnectorsIntegrationTest
