#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hdfs-13039
OUT_DIR=$HOME/tmp/bytecode/$case_name/sootOutput
SRC_DIR=$SCRIPT_DIR/../../systems/$case_name
diff_log="${SCRIPT_DIR}/../../ground_truth/$case_name/diff_log.txt"
alldirs=$SCRIPT_DIR/../runtime/target/classes
for i in `find $SRC_DIR/hadoop-hdfs-project -name "classes"`; do alldirs="$i $alldirs"; done
for i in `find $SRC_DIR/hadoop-common-project -name "classes"`; do alldirs="$i $alldirs"; done
#for i in `find $SRC_DIR/hadoop-hdfs-project -name "test-classes"`; do alldirs="$i $alldirs"; done
#for i in `find $SRC_DIR/hadoop-common-project -name "test-classes"`; do alldirs="$i $alldirs"; done

rm -rf ${OUT_DIR}
mkdir -p ${OUT_DIR}

JAVA_OPTS="-Danalysis.prefix=org.apache.hadoop -Danalysis.distributedMode=true" \
$SCRIPT_DIR/analyzer.sh \
-o $HOME/tmp/bytecode/$case_name/sootOutput \
-i $alldirs \
-fc $case_name \
-m org.apache.hadoop.hdfs.server.namenode.NameNode \
-sm org.apache.hadoop.hdfs.server.namenode.SecondaryNameNode org.apache.hadoop.hdfs.server.datanode.DataNode \
-fld $diff_log \

