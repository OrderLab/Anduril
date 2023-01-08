#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

$workspace/stop-datanode.sh 1
$workspace/stop-datanode.sh 2
$workspace/stop-datanode.sh 3
$workspace/stop-datanode.sh 4
$workspace/stop-namenode.sh

