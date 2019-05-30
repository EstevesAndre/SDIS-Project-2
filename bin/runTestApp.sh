#! /bin/sh

# if [ $# -ne 2 -a $# -ne 3 ]; then
#     echo "Usage: $0 <sub_protocol> <opnd_1> <opnd_2>?"
#     exit 1
# fi

java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 service/TestApp 192.168.43.78 9877 $@
