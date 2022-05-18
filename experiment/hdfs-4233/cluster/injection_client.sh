#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

pushd $HOME/charybdefs
./inject_client --pattern "/home/haoze/flaky-reproduction/experiment/hdfs-4233/cluster/fuser/current/edits_inprogress_\d+" --full create
popd
