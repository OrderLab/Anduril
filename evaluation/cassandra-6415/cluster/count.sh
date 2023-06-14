#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"


for i in {0..22941}; do
    c=`grep -r "flaky record injection $i " | wc -l`
    if [ $c -gt 1000 ]; then
        echo id:$i:count:$c
    fi
done 
