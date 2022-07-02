#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hbase-19608
OUT_DIR=$HOME/tmp/bytecode/$case_name/sootOutput
SRC_DIR=$SCRIPT_DIR/../../systems/$case_name
diff_log="${SCRIPT_DIR}/../../ground_truth/$case_name/diff_log.txt"
alldirs=$SCRIPT_DIR/../runtime/target/classes
for i in `find $SRC_DIR -name "classes"`; do alldirs="$i $alldirs"; done
# must exclude some, otherwise loading bytecode gets silent errors
for i in `find $SRC_DIR -name "test-classes"`; do
  if [[ "$i" != *"hbase-archetypes"* ]]; then
    alldirs="$i $alldirs"
  fi
done

extras=""
for i in `head -n1 $SRC_DIR/hbase-build-configuration/target/cached_classpath.txt|tr ':' '\n'`; do
  if [[ "$i" == *"hadoop-common"* ]]; then
    extras="$i $extras"
  fi
  if [[ "$i" == *"hbase-shaded-protobuf"* ]]; then
    extras="$i $extras"
  fi
  if [[ "$i" == *"protobuf-java"* ]]; then
    extras="$i $extras"
  fi
  if [[ "$i" == *"hbase-shaded-miscellaneous"* ]]; then
    extras="$i $extras"
  fi
done

rm -rf ${OUT_DIR}
mkdir -p ${OUT_DIR}

JAVA_OPTS="-Danalysis.prefix=org.apache.hadoop.hbase" \
$SCRIPT_DIR/analyzer.sh \
-o $HOME/tmp/bytecode/$case_name/sootOutput \
-x $extras \
-i $alldirs \
-fc $case_name \
-fld $diff_log \

