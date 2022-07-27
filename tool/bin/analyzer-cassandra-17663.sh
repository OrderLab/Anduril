#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=cassandra-17663
OUT_DIR=$HOME/tmp/bytecode/$case_name/sootOutput
SRC_DIR=$SCRIPT_DIR/../../systems/$case_name
diff_log="${SCRIPT_DIR}/../../ground_truth/$case_name/diff_log.txt"
alldirs=$SCRIPT_DIR/../runtime/target/classes
for i in `ls $SRC_DIR/build/classes`; do alldirs="$SRC_DIR/build/classes/$i $alldirs"; done
alldirs="$SRC_DIR/build/test/classes $alldirs"

extras=""
for i in `find $SRC_DIR/build/lib/jars/ -name "*.jar"`; do extras="$i $extras"; done
for i in `find $SRC_DIR/build/test/lib/jars/ -name "*.jar"`; do extras="$i $extras"; done


rm -rf ${OUT_DIR}
mkdir -p ${OUT_DIR}

JAVA_OPTS="-Danalysis.prefix=org.apache.cassandra" \
$SCRIPT_DIR/analyzer.sh \
-o $HOME/tmp/bytecode/$case_name/sootOutput \
-i $alldirs \
-fc $case_name \
-fld $diff_log \
-x $extras \
