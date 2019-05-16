package threads;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import source.ChordNode;

public class Listener implements Runnable {

    private ChordNode node;

    public Listener(ChordNode node) {
        this.node = node;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(this.node.getPort());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("> Received info");

            }
        } catch (IOException e) {
            System.err.println("Error listening on server socket");
            e.printStackTrace();
        }
    }
}