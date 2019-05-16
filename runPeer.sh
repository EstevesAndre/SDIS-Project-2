#! /bin/sh

# if [ $# -ne 2 ]; then
#     echo "Usage: $0 <address_ip>? <address_port>"
#     exit 1
# fi

java -Djavax.net.ssl.keyStore=SDIS -Djavax.net.ssl.keyStorePassword=123456 -classpath bin service/Peer