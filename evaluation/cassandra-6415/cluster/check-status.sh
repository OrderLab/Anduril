#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)
echo $workspace
grep 'Startup complete' $workspace/logs-*/system.log
