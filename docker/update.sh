#!/bin/bash

docker stop neji-webservices

docker rm neji-webservices

docker rmi -f neji-webservices

docker build --force-rm=true --no-cache=true -t neji-webservices .

docker run -d -p 8017:8017 --name neji-webservices -v /opt/neji-resources:/opt/neji-resources neji-webservices /bin/bash
