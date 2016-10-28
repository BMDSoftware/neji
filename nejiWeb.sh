#!/bin/bash
cp=target/neji-2.0.0-server.jar:$CLASSPATH
MEMORY=6G
JAVA_COMMAND="java -Xmx$MEMORY -Dfile.encoding=UTF-8 -classpath $cp"
CLASS=pt.ua.tm.neji.web.cli.WebMain

$JAVA_COMMAND $CLASS $*
