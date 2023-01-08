#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

pushd $HOME/charybdefs
./inject_client --clear
popd
