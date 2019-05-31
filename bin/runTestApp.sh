#! /bin/sh

if [ $# -ne 4 -a $# -ne 5 ]; then
    echo "Usage: $0 <ip> <port> <sub_protocol> <opnd_1> <opnd_2>?"
    exit 1
fi

java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 service/TestApp $@