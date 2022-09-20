#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
feedback_path=$1

java -jar $feedback_path \
--diff \
-g $SCRIPT_DIR/good-run-log.txt \
-b $SCRIPT_DIR/bad-run-log.txt \
>diff_log_original.txt

java -jar $feedback_path \
--double-diff \
-g $SCRIPT_DIR/good-run-log.txt \
-b $SCRIPT_DIR/bad-run-log.txt \
-t $SCRIPT_DIR/good-run-log-2.txt \
>diff_log_dd.txt

java -jar $feedback_path \
--double-diff-set \
-g $SCRIPT_DIR/good-run-log.txt \
-b $SCRIPT_DIR/bad-run-log.txt \
-t $SCRIPT_DIR/good-run-log-2.txt \
>diff_log_dd_set.txt

echo "Original:"
cat diff_log_original.txt | wc -l
echo "Double diff"
cat diff_log_dd.txt | wc -l
echo "Double diff Set minus"
cat diff_log_dd_set.txt | wc -l








