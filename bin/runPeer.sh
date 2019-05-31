#! /bin/sh

if [ $# -ne 2 -a $# -ne 4 ]; then
    echo "Usage: $0 <address_ip> <address_port> (<know_address_ip> <known_address_port>)?"
    exit 1
fi

java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=123456 service/Peer $@