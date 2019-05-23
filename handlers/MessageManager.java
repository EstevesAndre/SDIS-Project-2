package handlers;

import java.math.BigInteger;

// Class used to create message requests

public abstract class MessageManager {

    public enum Type {
        STORED, PUTCHUNK, DELETE, GETCHUNK, CHUNK, REMOVED, JOINED, // first project types
        SUCCESSOR, PREDECESSOR, KEY, YOUR_PREDECESSOR, OK, ERROR
    }

    public static byte[] createHeader(Type type, BigInteger nodeID, String[] args) {
        String ID = null;
        if (nodeID != null)
            ID = nodeID.toString();

        switch (type) {
        case KEY:
            return (type + " " + ID).getBytes();
        case PREDECESSOR:
            if (args == null)
                return (type + " " + ID).getBytes();
            else
                return (type + " " + ID + " " + args[0] + " " + args[1]).getBytes();
        case YOUR_PREDECESSOR:
            return (type + " " + ID + " " + args[0] + " " + args[1]).getBytes();
        case SUCCESSOR:
            return (type + " " + ID + " " + args[0] + " " + args[1]).getBytes();
        case OK:
            return (type + "").getBytes();
        case ERROR:
            return (type + "").getBytes();
        default:
            throw new IllegalArgumentException("Invalid message type for the request: " + type);
        }
    }

    public static byte[] createRequest(Type type, String address) {
        return null;
        // return createHeader(type, address, null);
    }

    public static String[] parseResponse(byte[] response) {
        return new String(response).trim().split("\\s+");
    }

    public static byte[] createApplicationHeader(Type type, String fileID, int chunkNumber, int replicationDegree) {
        switch (type) {
        case PUTCHUNK:
            return (type + " " + fileID + " " + chunkNumber + " " + replicationDegree + "\r\n\r\n").getBytes();
        case STORED:
            return (type + " " + fileID + " " + chunkNumber).getBytes();
        case DELETE:
            return "OK".getBytes();
        case GETCHUNK:
            return "OK".getBytes();
        default:
            throw new IllegalArgumentException("Invalid message type for the request: " + type);
        }
    }

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