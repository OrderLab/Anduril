#!/bin/bash

cd "$(dirname "$0")"

for d in `ls -d */`;
do
  if [ -x $d/compile.sh ]; then
	  echo "compiling $d .."
    pushd $d > /dev/null
    ./compile.sh
    popd > /dev/null
  fi
done
