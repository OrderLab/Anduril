#!/usr/bin/env bash

workspace=$(cd "$(dirname "${BASH_SOURCE-$0}")"; pwd)

id=1
SRC=$workspace/src-$id

java -Dlog4j.configuration=file:$workspace/log4j.properties \
-cp $workspace/conf-$id:$root_dir/driver/datastax/3.1.4/target/datastax_3_1_4-1.0-jar-with-dependencies.jar \
edu.jhu.order.mcgray.datastax_3_1_4.CassandraClientMain $@
