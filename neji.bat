@echo off

set cp=target\neji-2.0.2-jar-with-dependencies.jar
set memory=6144m
set encoding=UTF-8
set class=pt.ua.tm.neji.cli.Main

:run

java -Xmx%memory% -ea -Dfile.encoding=%encoding% -classpath %cp% %class% %*

:eof