#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=kafka-10340
OUT_DIR=$HOME/tmp/bytecode/$case_name/sootOutput
SRC_DIR=$SCRIPT_DIR/../../systems/$case_name
diff_log="${SCRIPT_DIR}/../../ground_truth/$case_name/diff_log.txt"
alldirs=$SCRIPT_DIR/../runtime/target/classes
#for i in `find $SRC_DIR -name "classes"`; do alldirs="$i $alldirs"; done
#for i in `find $SRC_DIR -name "test-classes"`; do alldirs="$i $alldirs"; done

for i in `find $SRC_DIR -name "main"`; do
  if [[ "$i" == *"/classes/java/main" ]]; then
    alldirs="$i $alldirs";
  fi
  if [[ "$i" == *"/classes/scala/main" ]]; then
    alldirs="$i $alldirs";
  fi
done

for i in `find $SRC_DIR -name "test"`; do
  if [[ "$i" == *"/classes/java/test" ]]; then
    alldirs="$i $alldirs";
  fi
  if [[ "$i" == *"/classes/scala/test" ]]; then
    alldirs="$i $alldirs";
  fi
done





# more extra: /usr/lib/jvm/java-8-openjdk-amd64/jre/../lib/tools.jar

#extras=""
#for i in `head -n1 $SRC_DIR/target/cached_classpath.txt|tr ':' '\n'`; do
#  if [[ "$i" == *"/.m2/repository/"* ]]; then
#    extras="$i $extras"
#  fi
#done

extras="$SCRIPT_DIR"
for i in `find $SRC_DIR -name "dependant-libs-2.13.5"`; do
  for j in `find $i -name "*.jar"`; do
    if [[ "$j" != *"/slf4j-log4j12-1.7.30.jar" ]]; then
      extras="$j $extras";
    fi
  done
done
for i in `find $SRC_DIR -name "dependant-libs"`; do
  for j in `find $i -name "*.jar"`; do
    if [[ "$j" != *"/slf4j-log4j12-1.7.30.jar" ]]; then
      extras="$j $extras";
    fi
  done
done

extras="$SRC_DIR/core/build/dependant-libs-2.13.5/slf4j-log4j12-1.7.30.jar $extras"
rm -rf ${OUT_DIR}
mkdir -p ${OUT_DIR}

JAVA_OPTS="-Danalysis.prefix=org.apache.kafka -Danalysis.secondaryPrefix=kafka" \
$SCRIPT_DIR/analyzer.sh \
-o $HOME/tmp/bytecode/$case_name/sootOutput \
-i $alldirs \
-fc $case_name \
-fld $diff_log \
-x $extras \

