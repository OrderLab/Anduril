#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

pushd $SCRIPT_DIR >/dev/null

java -jar $SCRIPT_DIR/driver.jar \
--experiment \
--spec $SCRIPT_DIR/tree.json \
--path $SCRIPT_DIR/trials \
--config $SCRIPT_DIR/config.properties \
$@

popd >/dev/null

