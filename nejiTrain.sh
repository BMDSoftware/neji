#!/bin/bash
cp=target/neji-2.0.0-jar-with-dependencies.jar:$CLASSPATH
MEMORY=12G
JAVA_COMMAND="java -Xmx$MEMORY -Dfile.encoding=UTF-8 -classpath $cp"
CLASS=pt.ua.tm.neji.train.cli.TrainMain

$JAVA_COMMAND $CLASS $*
