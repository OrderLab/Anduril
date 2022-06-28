#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

java -jar $SCRIPT_DIR/target/defaultparser-1.0-SNAPSHOT-jar-with-dependencies.jar $@
