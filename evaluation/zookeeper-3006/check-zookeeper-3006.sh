#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

java -jar reporter-1.0-SNAPSHOT-jar-with-dependencies.jar -t $1 -b -n zookeeerp-3006
