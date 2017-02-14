@echo off

set cp=target/neji-2.1.0-SNAPSHOT-server.jar
set memory=6144m
set encoding=UTF-8
set class=pt.ua.tm.neji.web.cli.WebMain

:run

java -Xmx%memory% -ea -Dfile.encoding=%encoding% -classpath %cp% %class% %*

:eof