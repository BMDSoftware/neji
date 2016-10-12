@echo off

set cp=target\neji-2.0-jar-with-dependencies.jar
set memory=2048m
set encoding=UTF-8
set class=pt.ua.tm.neji.cli.Main

:run

java -Xmx%memory% -ea -Dfile.encoding=%encoding% -classpath %cp% %class% %*

:eof