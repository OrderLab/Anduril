#!/bin/bash

./gradlew clean
./gradlew jar
./gradlew streams:test --tests org.apache.kafka.streams.integration.EmitOnChangeIntegrationTest
