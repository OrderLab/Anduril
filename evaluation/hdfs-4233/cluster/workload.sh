#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

log4j_path=$workspace/conf-1
client_jar=/home/whz/McGray/hd_3_2_2/target/hd_3_2_2-1.0-jar-with-dependencies.jar
client_main=edu.jhu.order.mcgray.hd_3_2_2/GrayHDFSClientMain

java -cp $log4j_path:$client_jar $client_main $workspace/conf-1 $@

#args example: write /1 1
