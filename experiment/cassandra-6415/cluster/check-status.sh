#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

grep 'Startup complete' $workspace/logs-*/system.log
