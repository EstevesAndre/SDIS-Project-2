package handlers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import source.ChordNode;

// Class used to send and receive messages

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
            System.out.println("SOCKET NULL");
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
        case "BACKUPNH":
            return node.handleBackupNhRequest();
        case "RESTORE":
            return node.handleRestoreRequest();
        case "DELETE":
            return node.handleDeleteRequest();
        case "DELETENH":
            return node.handleDeleteNhRequest();
        case "RECLAIM":
            return node.handleReclaimRequest();
        case "STATE":
            return node.handleStateRequest();
        default:
            throw new IllegalArgumentException("Invalid message type for the request: " + received[0]);
        }
    }

    public static void backupRequest(String address, String port, String path, String repDegree) {
        // prepare request
        // using java NIO to open file and create chunks

        int rd = Integer.parseInt(repDegree);

        String fileID = null;
        ArrayList<Chunk> chunks = null;
        try {
            fileID = IOManager.getFileHashID(path);
            chunks = IOManager.splitFile(fileID, path, rd);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        System.out.println("Splited file");

        for (int i = 0; i < chunks.size(); i++) {
            byte[] header = MessageManager.createApplicationHeader(MessageManager.Type.PUTCHUNK, fileID,
                    chunks.get(i).getId(), rd);
            byte[] putChunk = new byte[header.length + chunks.get(i).getSize()];
            System.arraycopy(header, 0, putChunk, 0, header.length);
            System.arraycopy(chunks.get(i).getContent(), 0, putChunk, header.length, chunks.get(i).getSize());

            byte[] response = RequestManager.sendRequest(address, Integer.parseInt(port), putChunk);
            System.out.println(new String(response));
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Finished BACKUP");
    }

    public static void backupNhRequest(String address, String port, String path, String rd) {
    }

    public static void restoreRequest(String address, String port, String path) {
    }

    public static void deleteRequest(String address, String port, String path) {
    }

    public static void deleteNhRequest(String address, String port, String path) {
    }

    public static void reclaimRequest(String address, String port, String path) {
    }

    public static void stateRequest(String address, String port, String option) {
    }
}