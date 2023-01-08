#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

log4j_path=$workspace/conf-1
client_jar=/home/tonypan/ECapi/target/ECapi-1.0-SNAPSHOT-jar-with-dependencies.jar
client_main=GrayHDFSClientMain

java -cp $log4j_path:$client_jar $client_main $workspace/conf-1 $@

#args example: write /1 1
