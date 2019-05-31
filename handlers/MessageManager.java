package handlers;

import java.math.BigInteger;

// Class used to create message requests

public abstract class MessageManager {

    public enum Type {
        STORED, BACKUP, PUTCHUNK, DELETE_FILE, DELETE_CHUNK, GETCHUNK, CHUNK, REMOVED, JOINED, GET_FILE_INFO, FILE_INFO,
        GIVE_FILE_INFO, SAVE_FILE_INFO, // first
        // project
        // types
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
        case SUCCESSOR:
            return (type + " " + ID + " " + args[0] + " " + args[1]).getBytes();
        case OK:
        case ERROR:
            return (type + "").getBytes();
        default:
            throw new IllegalArgumentException("Invalid message type for the request: " + type);
        }
    }

    // TODO: Currently it is not being used. Remove?
    public static byte[] createRequest(Type type, String address) {
        return null;
        // return createHeader(type, address, null);
    }

    public static String[] parseResponse(byte[] response) {
        return new String(response).trim().split("\\s+");
    }

    public static byte[] createApplicationHeader(Type type, String fileID, BigInteger keyBigInteger, int chunkNumber,
            int replicationDegree) {

        switch (type) {
        case PUTCHUNK:
            return (type + " " + keyBigInteger + " " + chunkNumber + "\r\n\r\n").getBytes();
        case CHUNK:
            return (type + " " + chunkNumber + " " + "\r\n\r\n").getBytes();
        case BACKUP:
            return (type + " " + keyBigInteger + " " + chunkNumber + " " + replicationDegree + "\r\n\r\n").getBytes();
        case STORED:
        case GETCHUNK:
            return (type + " " + keyBigInteger).getBytes();
        case DELETE_FILE:
            return (type + " " + fileID).getBytes();
        case DELETE_CHUNK:
            return (type + " " + keyBigInteger).getBytes();
        case SAVE_FILE_INFO:
        case FILE_INFO:
            return (type + " " + keyBigInteger + " " + chunkNumber + " " + replicationDegree).getBytes();
        case GET_FILE_INFO:
        case GIVE_FILE_INFO:
            return (type + " " + keyBigInteger).getBytes();
        default:
            throw new IllegalArgumentException("Invalid message type for the request: " + type);
        }
    }
}