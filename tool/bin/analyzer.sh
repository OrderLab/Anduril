#!/usr/bin/env bash

JAVA=java
VERSION=1.0
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
ANALYZER_HOME="$(dirname "${SCRIPT_DIR}")"
ANALYZER_JAR="${ANALYZER_HOME}/analyzer/target/analyzer-${VERSION}-jar-with-dependencies.jar"
ANALYZER_MAIN=analyzer.AnalyzerMain


# add -e to generate .class
# remove -e to generate jimple
OPTS="-a wjtp.flaky -e -p jb use-original-names:true"
echo $JAVA_OPTS

if [ ! -z ${baseline+x} ]; then
  JAVA_OPTS="-Danalysis.baseline=true $JAVA_OPTS"
fi

if [ ! -z ${fate+x} ]; then
  JAVA_OPTS="-Danalysis.fate=true $JAVA_OPTS"
fi

if [ ! -z ${crashtuner+x} ]; then
  JAVA_OPTS="-Danalysis.crashtuner=true $JAVA_OPTS"
fi

if [ ! -z ${stacktrace+x} ]; then
  JAVA_OPTS="-Danalysis.stackTrace=true $JAVA_OPTS"
fi

"${JAVA}" -Danalyzer.logs.dir=${ANALYZER_HOME}/logs ${JAVA_OPTS} \
-Dlog4j.configuration=file:${ANALYZER_HOME}/conf/log4j.properties \
-cp "${ANALYZER_JAR}" \
${ANALYZER_MAIN} ${OPTS} $@
