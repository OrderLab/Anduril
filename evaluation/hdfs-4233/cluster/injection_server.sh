#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

mkdir -p $workspace/fuser
pushd $HOME/charybdefs
./charybdefs $workspace/store-1 -f -omodules=subdir,subdir=$workspace/fuser
popd
