package handlers;

// Class used to create message requests

public abstract class MessageManager {

    public enum Type {
        STORED, PUTCHUNK, DELETE, GETCHUNK, CHUNK, REMOVED, JOINED, // first project types
        SUCCESSOR, PREDECESSOR, KEY
    }

    public static byte[] createHeader(Type type, String address, String[] args) {
        switch (type) {
        case KEY:
        case PREDECESSOR:
            return (type + " " + address).getBytes();

        default:
            throw new IllegalArgumentException("Invalid message type for the request: " + type);
        }
    }

    public static byte[] createRequest(Type type, String address) {

        return createHeader(type, address, null);
    }

    public static String[] parseResponse(byte[] response) {
        return new String(response).trim().split("\\s+");
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