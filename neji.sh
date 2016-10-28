#!/bin/bash
cp=target/neji-2.1.0-SNAPSHOT-jar-with-dependencies.jar:$CLASSPATH
MEMORY=8G
JAVA_COMMAND="java -Xmx$MEMORY -Dfile.encoding=UTF-8 -classpath $cp"
CLASS=pt.ua.tm.neji.cli.Main

$JAVA_COMMAND $CLASS $*
