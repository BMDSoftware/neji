@echo off

set cp=target/neji-2.0.1-server.jar
set memory=6144m
set encoding=UTF-8
set class=pt.ua.tm.neji.web.cli.WebMain

:run

java -Xmx%memory% -ea -Dfile.encoding=%encoding% -classpath %cp% %class% %*

:eof