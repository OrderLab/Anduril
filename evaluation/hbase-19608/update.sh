#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

case_name=${PWD##*/}                    # to assign to a variable
case_name=${case_name:-/}               # to correct for the case where PWD=/

case_name=$(printf '%s\n' "${PWD##*/}") # to print to stdout
                                        # ...more robust than echo for unusual names
                                        #    (consider a directory named -e or -n)
rm -f $SCRIPT_DIR/good-run-log
rm -f $SCRIPT_DIR/bad-run-log
cp -r $SCRIPT_DIR/../../ground_truth/$case_name/good-run-* $SCRIPT_DIR/good-run-log
cp -r $SCRIPT_DIR/../../ground_truth/$case_name/bad-run-* $SCRIPT_DIR/bad-run-log
cp $SCRIPT_DIR/../../tool/feedback/target/feedback-*-dependencies.jar $SCRIPT_DIR/feedback.jar
cp $SCRIPT_DIR/../../tool/driver/target/driver-*-dependencies.jar $SCRIPT_DIR/driver.jar
