package source;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;

import threads.Listener;

public class ChordNode {

    private static final int FINGERS_SIZE = 32;

    private final String Id;
    private final String address;
    private final int port;
    private String existingChordNode;
    private HashMap<Integer, Finger> fingers;
    private Finger successor;
    private Finger predecessor;

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public ChordNode(String address, String port) throws Exception {
        this.address = address;
        this.Id = getAddressHashID(address + '_' + port);
        this.port = Integer.valueOf(port);
        existingChordNode = null;

        this.initialize();
    }

    public ChordNode(String address, String port, String existingAddress, String existingPort) throws Exception {
        this.address = address;
        this.Id = getAddressHashID(address + '_' + port);
        this.port = Integer.valueOf(port);
        existingChordNode = getAddressHashID(existingAddress + '_' + existingPort);

        this.initialize();
    }

    private void initialize() {
        System.out.println("Chord node:\n - Address -> " + address + "\n - Port -> " + port);

        fingers = new HashMap<Integer, Finger>(FINGERS_SIZE);

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
        return Id;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public static String getAddressHashID(String toHash) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        return bytesToHex(digest.digest(toHash.getBytes(StandardCharsets.UTF_8)));
    }

    public static String bytesToHex(byte[] bytes) throws Exception {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void initializeFingers() {

        if (this.existingChordNode == null) { // first node
            for (int i = 0; i < FINGERS_SIZE; i++)
                this.fingers.put(i, new Finger(this.address, this.port));
            System.out.println("Finger table created");
        } else {
            // join node to ring
            // TODO
        }
    }

    private void initializeSuccessors() {

    }

    // private void setIndexFinger(int index,

    private void initializeThreads() {
        new Thread(new Listener(this)).start();
    }

}