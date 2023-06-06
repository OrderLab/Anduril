#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

MC=$HOME/Legolas
id=$1
cmd=$2
num=$3
client_jar=$MC/driver/datastax/3.1.4/target/datastax_3_1_4-1.0-jar-with-dependencies.jar
client_main=edu.jhu.order.mcgray.datastax_3_1_4.CassandraClientMain
port=9047
host=127.0.0.$id

java -Dlog4j.configuration=file:$workspace/conf-1/log4j.properties -cp $client_jar $client_main $cmd $num $host $port

#args example: 1 create 5
