package threads;

import java.io.IOException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import source.ChordNode;
import threads.Request;

public class Listener implements Runnable {

    private ChordNode node;
    private SSLServerSocket sslServerSocket;

    public Listener(ChordNode node) {
        this.node = node;

        try {
            SSLServerSocketFactory ssServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            sslServerSocket = (SSLServerSocket) ssServerSocketFactory.createServerSocket(this.node.getPort());
            sslServerSocket.setEnabledCipherSuites(sslServerSocket.getSupportedCipherSuites());

        } catch (IOException e) {
            if (ChordNode.debug)
                System.out.println(
                        "ERROR : serverSocketFactory biding...\nMaybe your port is already in use, check your input parameters!");
            e.printStackTrace();
            System.exit(-2);
        }

        // sslServerSocket.setNeedClientAuth(true);
    }

    @Override
    public void run() {
        try {
            SSLSocket sslSocket;

            while (true) {
                sslSocket = (SSLSocket) this.sslServerSocket.accept();
                new Thread(new Request(this.node, sslSocket)).start();
            }
        } catch (IOException e) {
            if (ChordNode.debug)
                System.err.println("Error listening on server socket");
            e.printStackTrace();
        }
    }
}