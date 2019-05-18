# SDIS-Project-2
Distributed Backup Service for the Internet

[Wikipedia info](https://en.wikipedia.org/wiki/Chord_(peer-to-peer))

### Compile

* Linux
    ./compile.sh

* Windows
    .\win-compile.bat

### Run

java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=123456 service/Peer $@

java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 service/TestApp $@