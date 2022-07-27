#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=cassandra-17663
ca_dir=$SCRIPT_DIR/../../systems/$case_name
target_dir="$HOME/tmp/bytecode/$case_name/classes"
sootoutput_dir="$HOME/tmp/bytecode/$case_name/sootOutput"

rm -rf $target_dir

cp -r $sootoutput_dir $target_dir
rm -r $target_dir/runtime

for i in `cat $SCRIPT_DIR/default-$case_name.txt`; do
  if [[ "$i" == *"/src/java/"* ]]; then
    q=$(echo "$i"|sed 's#/src/java/#/build/classes/main/#')
    p=$(echo "$i"|sed 's#^./src/java/##')
    rsync -a $ca_dir/$q $target_dir/$p
  elif [[ "$i" == *"/test/"* ]]; then
    p=$(echo ${i#*org})
    p="org$p"
    q="build/test/classes/$p"
    rsync -a $ca_dir/$q $target_dir/$p
  else
    echo "error: $i"
  fi
done

for i in `find $ca_dir -name *.class`; do
    if [[ "$i" == *"/build/test/classes/"* ]]; then
        p=$(echo ${i#*classes/})
        rsync -a $i $target_dir/$p
    fi
done


rsync -ra $SCRIPT_DIR/../runtime/target/runtime-1.0-jar-with-dependencies.jar $target_dir
