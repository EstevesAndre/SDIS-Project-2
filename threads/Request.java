package threads;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.net.ssl.SSLSocket;

import handlers.RequestManager;
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

        /*
         * InputStream input = null; OutputStream output = null; try { input =
         * talkSocket.getInputStream(); String request =
         * Helper.inputStreamToString(input); String response = processRequest(request);
         * if (response != null) { output = talkSocket.getOutputStream();
         * output.write(response.getBytes()); } input.close(); } catch (IOException e) {
         * throw new RuntimeException(
         * "Cannot talk.\nServer port: "+local.getAddress().getPort()+"; Talker port: "
         * +talkSocket.getPort(), e); }
         */

        ObjectInputStream input = null;
        ObjectOutputStream output = null;

        try {
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            if (ChordNode.debug)
                System.out.println(
                        "Error creating input and/or output streams for socket connection OR ailed reading object from socket stream");
            e.printStackTrace();
        }

        byte[] read = null;

        try {
            read = (byte[]) input.readObject();
        } catch (IOException e) {
            throw new RuntimeException("Failed reading object from socket stream.", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found locally, check serialVersionUID.", e);
        } catch (ClassCastException e) {
            throw new RuntimeException("Failed casting read Object to Message.", e);
        }

        // sends request
        byte[] response = RequestManager.handleRequest(node, read);
        // waits for response
        try {
            // long id = Thread.currentThread().getId();
            // System.out.println("\nAQUI CRL\n" + id);
            if (response != null)// && !socket.isOutputShutdown() && !socket.isClosed() && socket.isConnected())
                output.writeObject(response);
            Thread.sleep(50);
            input.close();
            output.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed writing object to socket stream.", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}