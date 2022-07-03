#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=hbase-15252
OUT_DIR=$HOME/tmp/bytecode/$case_name/sootOutput
SRC_DIR=$SCRIPT_DIR/../../systems/$case_name
diff_log="${SCRIPT_DIR}/../../ground_truth/$case_name/diff_log.txt"
alldirs=$SCRIPT_DIR/../runtime/target/classes
for i in `find $SRC_DIR -name "classes"`; do alldirs="$i $alldirs"; done
for i in `find $SRC_DIR -name "test-classes"`; do alldirs="$i $alldirs"; done

# more extra: /usr/lib/jvm/java-8-openjdk-amd64/jre/../lib/tools.jar

extras=""
for i in `head -n1 $SRC_DIR/target/cached_classpath.txt|tr ':' '\n'`; do
  if [[ "$i" == *"/.m2/repository/"* ]]; then
    extras="$i $extras"
  fi
done

rm -rf ${OUT_DIR}
mkdir -p ${OUT_DIR}

JAVA_OPTS="-Danalysis.prefix=org.apache.hadoop.hbase" \
$SCRIPT_DIR/analyzer.sh \
-o $HOME/tmp/bytecode/$case_name/sootOutput \
-i $alldirs \
-fc $case_name \
-fld $diff_log \
-x $extras \
