package handlers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import source.ChordNode;
import source.Finger;

// Class used to send and receive messages

public abstract class RequestManager {

    public static SSLSocket makeConnection(String address, int port) throws IOException {
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(address, port);

        sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());

        return sslSocket;
    }

    private static SSLSocket send(Finger node, byte[] request) {

        SSLSocket socket = null;
        try {
            socket = makeConnection(node.getAddress(), node.getPort());
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return socket;
    }

    public static byte[] sendRequest(Finger predecessor, byte[] request) {

        SSLSocket socket = send(predecessor, request);
        byte[] response = null;

        try {
            Thread.sleep(50);
            response = getResponse(socket);
            socket.close();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private static byte[] getResponse(SSLSocket socket) {

        try {
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            return (byte[]) input.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
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
            return MessageManager.createHeader(MessageManager.Type.KEY, node.getID(), null);
        case "PREDECESSOR":
            return MessageManager.createHeader(MessageManager.Type.PREDECESSOR, node.getPredecessor().getID(), null);
        default:
            throw new IllegalArgumentException("Invalid message type for the request: " + received[0]);
        }

    }
}