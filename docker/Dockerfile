############################################################
# Dockerfile to build Neji Webservices
# Based on airdock/oracle-jdk:1.7
############################################################

# Set the base image to Ubuntu
FROM lwieske/java-8:jdk-8u77 
MAINTAINER David Campos

# Update and install unzip
RUN yum install -y unzip

# Install
ADD neji-server.zip /opt/neji-server.zip
WORKDIR /opt
RUN unzip neji-server.zip
RUN ls /opt
RUN rm -rf neji-server.zip
RUN chmod u+x neji-server/neji-server.sh

# Setup symbolic links for persistent data
RUN ln -s /opt/neji-resources /opt/neji-server/resources

# Expose the default port
EXPOSE 8017

# Add run.sh to Docker
ADD ./run.sh /opt/run.sh

# Set default container command
ENTRYPOINT ["/opt/run.sh"]
