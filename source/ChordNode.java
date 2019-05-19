package source;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import javax.net.ssl.SSLSocket;

import threads.CheckPredecessor;
import threads.CheckSuccessor;
import threads.Listener;
import handlers.IOManager;
import handlers.MessageManager;
import handlers.RequestManager;

public class ChordNode {

    private static final int FINGERS_SIZE = 8;

    private final Finger ID;
    private String existingChordNode;
    private String existingNodeAddress;
    private int existingNodePort;
    private HashMap<Integer, Finger> fingers;
    private Finger successor;
    private Finger predecessor;

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public ChordNode(String address, String port) throws Exception {
        this.ID = new Finger(address, Integer.parseInt(port));
        existingChordNode = null;

        this.initialize();
    }

    public ChordNode(String address, String port, String existingAddress, String existingPort) throws Exception {
        this.ID = new Finger(address, Integer.parseInt(port));
        this.existingNodeAddress = existingAddress;
        this.existingNodePort = Integer.valueOf(existingPort);
        this.existingChordNode = IOManager.getAddressHashID(existingAddress + '_' + existingPort);

        if (this.ID.getID().equals(this.existingChordNode))
            throw new IllegalArgumentException("Existing ID is equal to new chord node... Attemp failed!");

        this.initialize();
    }

    private void initialize() throws Exception {
        System.out.println("Chord node:\n - Address -> " + this.ID.getAddress() + "\n - Port -> " + this.ID.getPort());

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

    public Finger getID() {
        return this.ID;
    }

    public String getAddress() {
        return this.ID.getAddress();
    }

    public int getPort() {
        return this.ID.getPort();
    }

    private void initiateSystemConfigs() {
        System.setProperty("javax.net.ssl.keyStore", "keystore");
        System.setProperty("javax.net.ssl.trustStore", "truststore");
    }

    private void initializeFingers() throws Exception {

        if (this.existingChordNode == null) { // first node
            for (int i = 0; i < FINGERS_SIZE; i++)
                this.fingers.put(i, new Finger(this.ID.getAddress(), this.ID.getPort()));
        } else {
            // join node to ring
            System.out.println("Joining node " + this.existingChordNode);

            for (int i = 0; i < FINGERS_SIZE; i++) {
                System.out.println("Find " + i + "º finger!");
                byte[] request = MessageManager.createHeader(MessageManager.Type.SUCCESSOR, this.ID.getID(),
                        new String[] { this.getAddress(), String.valueOf(this.getPort()) });

                byte[] response = RequestManager.sendRequest(this.existingNodeAddress, this.existingNodePort, request);
                // [SUCCESSOR ID ADDRESS PORT]
                String[] parts = MessageManager.parseResponse(response);
                this.fingers.put(i, new Finger(parts[2], parts[3]));

                System.out.println("Received " + new String(response));
            }
        }

        System.out.println("Finger table created");
    }

    private void initializeSuccessors() {
        this.predecessor = this.fingers.get(0); // first
        this.successor = this.fingers.get(0); // first

        if (!this.ID.equals(this.getSuccessor())) {

        }
    }

    private void initializeThreads() {
        new Thread(new Listener(this)).start();
        new Thread(new CheckPredecessor(this, 5000)).start();
        new Thread(new CheckSuccessor(this, 2000)).start();
    }

    public void notifySuccessor() {
        if (this.getSuccessor().equals(this.ID))
            return;

        byte[] request = MessageManager.createHeader(MessageManager.Type.YOUR_PREDECESSOR, this.getID().getID(),
                new String[] { this.getAddress(), String.valueOf(this.getPort()) });
        byte[] response = RequestManager.sendRequest(this.getSuccessor().getAddress(), this.getSuccessor().getPort(),
                request);
        String isOK = new String(response);
        if (!isOK.equals("OK"))
            System.out.println("ERROR OCCURRED");
    }

    public byte[] handlePredecessorRequest(String[] received) {
        if (predecessor == null)
            return MessageManager.createHeader(MessageManager.Type.PREDECESSOR, "ERROR", null);
        else
            return MessageManager.createHeader(MessageManager.Type.PREDECESSOR, predecessor.getID(),
                    new String[] { predecessor.getAddress(), String.valueOf(predecessor.getPort()) });
    }

    public byte[] handleYourPredecessorRequest(String[] received) {
        // [YOUR_PREDECESSOR ID ADDRESS PORT]

        System.out.println("Notified by " + received[1]);

        Finger potential = null;
        try {
            potential = new Finger(received[2], received[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (this.predecessor == null) {
            this.predecessor = potential;
        }

        if (potential.comparator(predecessor, this.ID)) {
            this.predecessor = potential;
        }

        return MessageManager.createHeader(MessageManager.Type.OK, null, null);
    }

    public byte[] handleSuccessorRequest(String[] received) {

        if (received.length != 4)
            return MessageManager.createHeader(MessageManager.Type.ERROR, null, null);

        Finger looking = null;
        try {
            looking = new Finger(received[2], received[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Finger ret = findSuccessor(looking);

        if (ret == null)
            return MessageManager.createHeader(MessageManager.Type.ERROR, null, null);
        else
            return MessageManager.createHeader(MessageManager.Type.SUCCESSOR, ret.getID(),
                    new String[] { ret.getAddress(), String.valueOf(ret.getPort()) });
    }

    private Finger findSuccessor(Finger finger) {
        // if given finger is between this ID and his sucessor
        if (finger.comparator(this.ID, successor))
            return successor;

        for (int i = fingers.size() - 1; i > 0; i--) {
            Finger aux = fingers.get(i);
            if (aux != null && aux.comparator(this.ID, finger))
                return aux;
        }
        return ID;
    }
}