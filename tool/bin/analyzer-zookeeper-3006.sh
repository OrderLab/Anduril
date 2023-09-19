#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=zookeeper-3006
OUT_DIR=$HOME/tmp/bytecode/$case_name/sootOutput
SRC_DIR=$SCRIPT_DIR/../../systems/$case_name
diff_log="${SCRIPT_DIR}/../../ground_truth/$case_name/diff_log.txt"
classes_dir="$SRC_DIR/zookeeper-server/target/classes"
test_classes_dir="$SRC_DIR/zookeeper-server/target/test-classes"
runtime_classes_dir=$SCRIPT_DIR/../runtime/target/classes

rm -rf ${OUT_DIR}
mkdir -p ${OUT_DIR}

JAVA_OPTS="-Danalysis.badLog=${SCRIPT_DIR}/../../ground_truth/$case_name/bad-run-log.txt" $SCRIPT_DIR/analyzer.sh \
-o $HOME/tmp/bytecode/$case_name/sootOutput \
-i $classes_dir $test_classes_dir $runtime_classes_dir \
-fc $case_name \
-fld $diff_log \

