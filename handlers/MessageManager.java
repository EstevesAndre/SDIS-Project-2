package handlers;

import java.io.IOException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

// Class used to create message requests

public class MessageManager {

    public enum Type {
        STORED, PUTCHUNK, DELETE, GETCHUNK, CHUNK, REMOVED, JOINED, // first project types
        SUCCESSOR, PREDECESSOR
    }

    public String createHeader(Type type, String nodeID, String address, String[] args) {
        switch (type) {
        case SUCCESSOR:
            return type + " " + nodeID + " " + address;
        default:
            return "ERROR";
        }
    }

    public static SSLSocket makeConnection(String address, int port) throws IOException {
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(address, port);

        sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());

        return sslSocket;
    }

    // public String createHeader(String messageType, String fileID, int
    // chunkNumber, int replicationDegree) {
    // return messageType + " " + this.version + " " + this.peerID + " " + fileID +
    // " " + chunkNumber + " "
    // + replicationDegree + "\r\n\r\n";
    // }

    // public String createHeader(String messageType, String fileID, int
    // chunkNumber) {
    // return messageType + " " + this.version + " " + this.peerID + " " + fileID +
    // " " + chunkNumber + "\r\n\r\n";
    // }

    // public String createHeader(String messageType, String fileID) {
    // return messageType + " " + this.version + " " + this.peerID + " " + fileID +
    // "\r\n\r\n";
    // }

    // public String createHeader(String messageType) {
    // return messageType + " " + this.version + " " + this.peerID + "\r\n\r\n";
    // }
}