#!/bin/sh

wget http://127.0.0.1:15672/cli/rabbitmqadmin
chmod 744 rabbitmqadmin

./rabbitmqadmin declare user name=sportingsolutions@fern password=sporting tags=
./rabbitmqadmin declare vhost name=/fern
./rabbitmqadmin declare permission vhost=/fern user=sportingsolutions@fern  configure=.* write=.* read=.*

