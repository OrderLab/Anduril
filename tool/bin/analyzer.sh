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

"${JAVA}" -Danalyzer.logs.dir=${ANALYZER_HOME}/logs "${JAVA_OPTS}" \
-Dlog4j.configuration=file:${ANALYZER_HOME}/conf/log4j.properties \
-cp "${ANALYZER_JAR}" \
${ANALYZER_MAIN} ${OPTS} $@
