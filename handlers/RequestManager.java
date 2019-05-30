package handlers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import source.ChordNode;

public abstract class RequestManager {

    public static SSLSocket makeConnection(String address, int port) throws IOException {
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(address, port);
        sslSocket.setSoTimeout(2000);
        sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());

        return sslSocket;
    }

    private static SSLSocket send(String IPAddress, int IPPort, byte[] request) {

        SSLSocket socket = null;
        try {
            socket = makeConnection(IPAddress, IPPort);
            if (!socket.isConnected())
                return null;
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(request);
        } catch (IOException e) {
            // e.printStackTrace();
        }

        return socket;
    }

    public static byte[] sendRequest(String IPAddress, int IPPort, byte[] request) {

        SSLSocket socket = send(IPAddress, IPPort, request);

        if (socket == null) {
            // system.out.println("SOCKET NULL");
            return null;
        }
        byte[] response = null;

        try {
            Thread.sleep(100);
            response = getResponse(socket);
            socket.close();
        } catch (IOException e) {
            // e.printStackTrace();
        } catch (InterruptedException e) {
            // e.printStackTrace();
        }

        return response;
    }

    private static byte[] getResponse(SSLSocket socket) {

        byte[] response = null;

        try {
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            response = (byte[]) input.readObject();
        } catch (IOException e) {
            // e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // e.printStackTrace();
        }

        return response;
    }

    public static byte[] handleRequest(ChordNode node, byte[] request) {

        int splitIndex = 0;
        for (int i = 0; i < request.length - 3; i++) {
            if (request[i] == 13 && request[i + 1] == 10 && request[i + 2] == 13 && request[i + 3] == 10) { // \r\n\r\n
                splitIndex = i + 4;
                break;
            }
        }
        String[] received = null;
        if (splitIndex == 0)
            received = new String(request, StandardCharsets.UTF_8).trim().split("\\s+");
        else
            received = new String(request, 0, splitIndex).trim().split("\\s+");

        switch (received[0]) {
        case "KEY":
            return MessageManager.createHeader(MessageManager.Type.KEY, node.getKey().getID(), null);
        case "PREDECESSOR":
            return node.handlePredecessorRequest(received);
        case "SUCCESSOR":
            return node.handleSuccessorRequest(received);
        case "YOUR_PREDECESSOR":
            return node.handleYourPredecessorRequest(received);
        case "PUTCHUNK":
            return node.handlePutchunkRequest(request);
        case "BACKUP":
            return node.handleBackupRequest(request);
        case "GETCHUNK":
            return node.handleGetChunkRequest(received);
        case "DELETE_FILE":
            return node.handleDeleteRequest(received[1]);
        case "DELETE_CHUNK":
            return node.deleteChunk(received[1]);
        case "RECLAIM":
            return node.handleReclaimRequest();
        case "STATE":
            return node.handleStateRequest();
        case "GET_FILE_INFO":
            return node.handleGetFileInfo(received);
        case "FILE_INFO":
            return node.handleSaveFileInfoRequest(received);
        default:
            throw new IllegalArgumentException("Invalid message type for the request: " + received[0]);
        }
    }

    public static void backupRequest(String address, String port, String file, String repDegree) {
        // prepare request
        // using java NIO to open file and create chunks

        int rd = Integer.parseInt(repDegree);

        String fileID = IOManager.getFileHashID(file);
        ArrayList<Chunk> chunks = IOManager.splitFile(fileID, file, rd);
        System.out.println("Splited file " + fileID);

        for (int i = 0; i < chunks.size(); i++) {
            for (int j = 0; j < rd; j++) {
                BigInteger chunkID = IOManager.getStringHashed(fileID + i + j).shiftRight(1);

                byte[] header = MessageManager.createApplicationHeader(MessageManager.Type.BACKUP, null, chunkID,
                        chunks.get(i).getId(), rd);
                byte[] putChunk = new byte[header.length + chunks.get(i).getSize()];
                System.arraycopy(header, 0, putChunk, 0, header.length);
                System.arraycopy(chunks.get(i).getContent(), 0, putChunk, header.length, chunks.get(i).getSize());

                byte[] response = RequestManager.sendRequest(address, Integer.parseInt(port), putChunk);

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (response == null) {
                    System.err.println("Failed to connect...");
                    return;
                }

                if ((new String(response)).startsWith("STORED"))
                    System.out.println("Successfully stored chunk " + i + " with rd = " + j);
                else {
                    System.err.println("Failed to store chunk " + i + " with rd = " + j);
                    return;
                }

            }
        }

        // Sends file info to be store in the ring
        byte[] fileInfo = MessageManager.createApplicationHeader(MessageManager.Type.FILE_INFO, null,
                IOManager.getStringHashed(file), chunks.size(), rd);
        byte[] response = RequestManager.sendRequest(address, Integer.parseInt(port), fileInfo);

        if ((new String(response)).startsWith("STORED"))
            System.out.println("Successfully stored chunk info");
        else {
            System.err.println("Failed to store chunk info");
            return;
        }

        System.out.println("Finished BACKUP");
    }

    public static void restoreRequest(String address, String port, String file) {
        byte[] fileInfo = MessageManager.createApplicationHeader(MessageManager.Type.GET_FILE_INFO, null,
                IOManager.getStringHashed(file), 0, 0);
        byte[] response = RequestManager.sendRequest(address, Integer.parseInt(port), fileInfo);

        if (response == null) {
            System.out.println("Failed connection");
            return;
        }

        String[] parts = MessageManager.parseResponse(response);

        if (parts[0] == "ERROR")
            throw new IllegalArgumentException("Received error to GET_FILE_INFO");

        System.out.println("Received " + new String(response));

        String fileID = IOManager.getFileHashID(file);
        int numberOfChunks = Integer.parseInt(parts[2]), rd = Integer.parseInt(parts[3]);

        ConcurrentHashMap<Integer, byte[]> chunks = new ConcurrentHashMap<>();

        for (int i = 0; i < numberOfChunks; i++) {
            for (int j = 0; j < rd; j++) {
                BigInteger chunkID = IOManager.getStringHashed(fileID + i + j).shiftRight(1);
                byte[] getChunk = MessageManager.createApplicationHeader(MessageManager.Type.GETCHUNK, null, chunkID, 0,
                        0);
                byte[] chunk = RequestManager.sendRequest(address, Integer.parseInt(port), getChunk);

                if (chunk == null) {
                    System.out.println("Failed connection");
                    return;
                }

                // store chunk here
                int splitIndex = 0;
                for (int k = 0; k < chunk.length; k++) {
                    if (chunk[k] == 13) {
                        splitIndex = k + 4;
                        break;
                    }
                }

                byte[] chunkcontent = Arrays.copyOfRange(chunk, splitIndex, chunk.length);
                String[] chunkParts = new String(chunk, 0, splitIndex).trim().split("\\s+");

                int chunkNr = Integer.parseInt(chunkParts[1]);

                System.out.println(chunkParts[1] + " nr: " + chunkNr + " " + chunkcontent.length);
                chunks.put(chunkNr, chunkcontent);

                if (chunks.size() == numberOfChunks)
                    break;
            }
        }

        IOManager.restoreFile(file, chunks);
        System.out.println("RESTORE completed");
    }

    public static void deleteRequest(String address, String port, String filename) {
        if (filename == null) {
            if (ChordNode.debug2)
                System.err.println("Wrong number of arguments for DELETE operation\n");
            return;
        }

        String fileID = IOManager.getFileHashID(filename);

        byte[] request = MessageManager.createApplicationHeader(MessageManager.Type.DELETE_FILE, filename, null, 0, 0);
        byte[] response = RequestManager.sendRequest(address, Integer.parseInt(port), request);

        if (response == null) {
            if (ChordNode.debug2)
                System.out.println("Couldn't connect to the given Address + Port");
            return;
        }

        String strResponse = new String(response);

        if (ChordNode.debug2) {
            if (strResponse.equals("ERROR"))
                System.out.println("File " + filename + " not deleted");
            else
                System.out.println("File " + filename + " DELETED!");
        }
    }

    public static void reclaimRequest(String address, String port, String path) {
    }

    public static void stateRequest(String address, String port, String option) {
    }
}