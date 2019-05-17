package threads;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLSocket;

import source.ChordNode;

public class Request implements Runnable {

    private ChordNode node;
    private SSLSocket socket;

    public Request(ChordNode node, SSLSocket socket) {
        this.node = node;
        this.socket = socket;
    }

    @Override
    public void run() {

        ObjectInputStream input = null;
        ObjectOutputStream output = null;

        try {
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println(
                    "Error creating input and/or output streams for socket connection OR ailed reading object from socket stream");
            e.printStackTrace();
        }

        byte[] read = null;

        try {
            read = (byte[]) input.readObject();
            System.out.println("Receveid message: " + new String(read, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Failed reading object from socket stream.", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found locally, check serialVersionUID.", e);
        } catch (ClassCastException e) {
            throw new RuntimeException("Failed casting read Object to Message.", e);
        }

        // sends request
        // waits for response
        // and sends back to output
    }
}