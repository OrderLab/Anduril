#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"


for i in {0..295406}; do
    c=`grep "flaky record injection $i " output.txt | wc -l`
    if [ $c -gt 1000 ]; then
        echo id:$i:count:$c
    fi
done 
