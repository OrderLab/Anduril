#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=kafka-12508
hd_dir=$SCRIPT_DIR/../../systems/$case_name
target_dir="$HOME/tmp/bytecode/$case_name/classes"
sootoutput_dir="$HOME/tmp/bytecode/$case_name/sootOutput"

rm -rf $target_dir

cp -r $sootoutput_dir $target_dir
rm -r $target_dir/runtime

for i in `cat $SCRIPT_DIR/default-$case_name.txt`; do
  if [[ "$i" == *"/src/main/java/"* ]]; then
    q=$(echo "$i"|sed 's#/src/main/java/#/build/classes/java/main/#')
    p=$(echo "$i"|sed 's#^./.*/src/main/java/##')
    rsync -a $hd_dir/$q $target_dir/$p
  elif [[ "$i" == *"/src/test/java/"* ]]; then
    q=$(echo "$i"|sed 's#/src/test/java/#/build/classes/java/test/#')
    p=$(echo "$i"|sed 's#^./.*/src/test/java/##')
    rsync -a $hd_dir/$q $target_dir/$p
  else
    echo "error: $i"
  fi
done
#./core/build/classes/scala/test/kafka/test/ClusterInstance.class
rsync -a $hd_dir/./core/build/classes/scala/test/kafka/test/ClusterInstance.class $target_dir/kafka/test/ClusterInstance.java


rsync -ra $SCRIPT_DIR/../runtime/target/runtime-1.0-jar-with-dependencies.jar $target_dir
