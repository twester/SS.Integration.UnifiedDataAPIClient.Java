FROM ubuntu:latest
MAINTAINER Sporting Solutions

# Install RabbitMq preReqs
RUN apt-get install -y wget
RUN apt-get install -y erlang-nox
RUN apt-get install -y logrotate


# Install RabbitMq
RUN wget --no-check-certificate https://www.rabbitmq.com/releases/rabbitmq-server/v3.1.4/rabbitmq-server_3.1.4-1_all.deb

RUN sudo dpkg -i rabbitmq-server_3.1.4-1_all.deb
RUN sudo rabbitmq-plugins enable rabbitmq_management


# Configure access to the container
EXPOSE 5672 15672
ENTRYPOINT ["/usr/sbin/rabbitmq-server"]
