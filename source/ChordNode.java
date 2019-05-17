package source;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import javax.net.ssl.SSLSocket;

import threads.Listener;
import handlers.IOManager;
import handlers.MessageManager;

public class ChordNode {

    private static final int FINGERS_SIZE = 32;

    private final String ID;
    private final String address;
    private final int port;
    private String existingChordNode;
    private String existingNodeAddress;
    private int existingNodePort;
    private HashMap<Integer, Finger> fingers;
    private Finger successor;
    private Finger predecessor;

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public ChordNode(String address, String port) throws Exception {
        this.address = address;
        this.ID = IOManager.getAddressHashID(address + '_' + port);
        this.port = Integer.valueOf(port);
        existingChordNode = null;

        this.initialize();
    }

    public ChordNode(String address, String port, String existingAddress, String existingPort) throws Exception {
        this.address = address;
        this.ID = IOManager.getAddressHashID(address + '_' + port);
        this.port = Integer.valueOf(port);
        this.existingNodeAddress = existingAddress;
        this.existingNodePort = Integer.valueOf(existingPort);
        this.existingChordNode = IOManager.getAddressHashID(existingAddress + '_' + existingPort);

        this.initialize();
    }

    private void initialize() {
        System.out.println("Chord node:\n - Address -> " + address + "\n - Port -> " + port);

        fingers = new HashMap<Integer, Finger>(FINGERS_SIZE);
        initiateSystemConfigs();

        this.initializeFingers();
        this.initializeSuccessors();
        this.initializeThreads();
    }

    public Finger getSuccessor() {
        return successor;
    }

    public void setSuccessor(Finger newSuccessor) {
        this.successor = newSuccessor;
    }

    public Finger getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(Finger newPredecessor) {
        this.predecessor = newPredecessor;
    }

    public String getId() {
        return ID;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    private void initiateSystemConfigs() {
        System.setProperty("javax.net.ssl.keyStore", "keystore");
        System.setProperty("javax.net.ssl.trustStore", "truststore");
    }

    private void initializeFingers() {

        if (this.existingChordNode == null) { // first node
            for (int i = 0; i < FINGERS_SIZE; i++)
                this.fingers.put(i, new Finger(this.address, this.port));
        } else {
            // join node to ring
            // TODO
        }

        System.out.println("Finger table created");
    }

    private void initializeSuccessors() {

        if (this.existingChordNode == null) {
            // first node
            this.predecessor = this.fingers.get(0); // first
            this.successor = this.fingers.get(0); // first

        } else {
            // join node to ring on existingChordNode given
            if (this.address.equals(this.existingChordNode)) {
                System.out.println("Wrong request, successor of himself");
                return;
            }

            System.out.println("Joining node " + this.existingChordNode);
            SSLSocket socket = null;

            try {
                socket = MessageManager.makeConnection(this.existingNodeAddress, this.existingNodePort);

                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject("CARALHOOOO".getBytes());
                socket.close();

            } catch (IOException e) {
                throw new RuntimeException("Failed connecting to server socket.", e);
            }

        }
    }

    private void initializeThreads() {
        new Thread(new Listener(this)).start();
        // new Thread(new )
    }

}