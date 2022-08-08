#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

$workspace/start-namenode.sh
./client.sh ec -enablePolicy -policy XOR-2-1-1024k
$workspace/start-datanode.sh 2
$workspace/start-datanode.sh 3
$workspace/start-datanode.sh 4
$workspace/start-datanode.sh 5
