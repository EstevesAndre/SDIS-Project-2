package source;

import java.util.HashMap;

import threads.CheckPredecessor;
import threads.CheckSuccessor;
import threads.Listener;

import handlers.IOManager;
import handlers.MessageManager;
import handlers.RequestManager;

public class ChordNode {

    /**
     * Size of finger table
     */
    private static final int FINGERS_SIZE = 8;

    /**
     * Unique identifier
     */
    private final Finger ID;

    /**
     * Existing chord node identifier
     */
    private String existingChordNode;

    /**
     * Exisitng chord node address
     */
    private String existingNodeAddress;

    /**
     * Existing chord node port
     */
    private int existingNodePort;

    /**
     * finger table
     */
    private HashMap<Integer, Finger> fingers;

    /**
     * Successor of this node
     */
    private Finger successor;

    /**
     * Predecessor of this node
     */
    private Finger predecessor;

    /**
     * Constructor of the first chord node to enter the ring (starting node)
     * 
     * @param address Address of the machine
     * @param port    Port to comunicate
     * @throws Exception
     */
    public ChordNode(String address, String port) throws Exception {
        this.ID = new Finger(address, Integer.parseInt(port));
        existingChordNode = null;

        this.initialize();
    }

    /**
     * Construtor of the next nodes to enter the ring given the existing node
     * 
     * @param address         Address of the machine
     * @param port            Port to comunicate
     * @param existingAddress Exisiting address to comunicate
     * @param existingPort    Existing port to comunicate
     * @throws Exception
     */
    public ChordNode(String address, String port, String existingAddress, String existingPort) throws Exception {
        this.ID = new Finger(address, Integer.parseInt(port));
        this.existingNodeAddress = existingAddress;
        this.existingNodePort = Integer.valueOf(existingPort);
        this.existingChordNode = IOManager.getAddressHashID(existingAddress + '_' + existingPort);

        if (this.ID.getID().equals(this.existingChordNode))
            throw new IllegalArgumentException("Existing ID is equal to new chord node... Attemp failed!");

        this.initialize();
    }

    /**
     * Initial function to start up the class Creates finger table - Initiate system
     * configs - Initiate sucessors - Initiate and Start threads
     * 
     * @throws Exception
     */
    private void initialize() throws Exception {
        System.out.println("Chord node:\n - Address -> " + this.ID.getAddress() + "\n - Port -> " + this.ID.getPort());

        fingers = new HashMap<Integer, Finger>(FINGERS_SIZE);
        initiateSystemConfigs();

        this.initializeFingers();
        this.initializeSuccessors();
        this.initializeThreads();
    }

    /**
     * 
     * @return (Finger) sucessor
     */
    public Finger getSuccessor() {
        return successor;
    }

    /**
     * Sets the successor
     * 
     * @param newSuccessor Next Successor
     */
    public void setSuccessor(Finger newSuccessor) {
        this.successor = newSuccessor;
    }

    /**
     * 
     * @return (Finger) predecessor
     */
    public Finger getPredecessor() {
        return predecessor;
    }

    /**
     * Sets the predecessor
     * 
     * @param newPredecessor Next predecessor
     */
    public void setPredecessor(Finger newPredecessor) {
        this.predecessor = newPredecessor;
    }

    /**
     * 
     * @return (Finger) Unique Identifier
     */
    public Finger getID() {
        return this.ID;
    }

    /**
     * 
     * @return Unique Identifier address
     */
    public String getAddress() {
        return this.ID.getAddress();
    }

    /**
     * 
     * @return Unique Identifier port
     */
    public int getPort() {
        return this.ID.getPort();
    }

    /**
     * Sets the properties for the SSL
     */
    private void initiateSystemConfigs() {
        System.setProperty("javax.net.ssl.keyStore", "keystore");
        System.setProperty("javax.net.ssl.trustStore", "truststore");
    }

    /**
     * Initialize fingers If first node, all fingers are equal this Otherwise sends
     * requests to the existing node to get the successor
     * 
     * @throws Exception
     */
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

    /**
     * Sets the successor and predecessor
     */
    private void initializeSuccessors() {
        this.predecessor = this.fingers.get(0); // first
        this.successor = this.fingers.get(0); // first

        if (!this.ID.equals(this.getSuccessor())) {

        }
    }

    /**
     * initialize threads, Listener, CheckPredecessor and Successor
     */
    private void initializeThreads() {
        new Thread(new Listener(this)).start();
        new Thread(new CheckPredecessor(this, 5000)).start();
        new Thread(new CheckSuccessor(this, 2000)).start();
    }

    /**
     * Notify sucessor that this node is his predecessor
     */
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

    /**
     * Handle PREDECESSOR request
     * 
     * @param received arguments received
     * @return message with the response, nodeID, nodeAddress and nodePort
     */
    public byte[] handlePredecessorRequest(String[] received) {
        if (predecessor == null)
            return MessageManager.createHeader(MessageManager.Type.PREDECESSOR, "ERROR", null);
        else
            return MessageManager.createHeader(MessageManager.Type.PREDECESSOR, predecessor.getID(),
                    new String[] { predecessor.getAddress(), String.valueOf(predecessor.getPort()) });
    }

    /**
     * Handle YOUR_PREDECESSOR request
     * 
     * @param received arguments received
     * @return message with the response, OK
     */
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

    /**
     * Handle SUCCESSOR request
     * 
     * @param received arguments received
     * @return message with ERROR or Successor, ID, Address and Port
     */
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

    /**
     * Finds the successor of finger request
     * 
     * @param finger Finger to find his successor
     * @return found successor
     */
    private Finger findSuccessor(Finger finger) {
        // if given finger is between this ID and his sucessor
        if (successor == null)
            return ID;

        if (finger.comparator(ID, successor))
            return successor;

        for (int i = fingers.size() - 1; i > 0; i--) {
            Finger aux = fingers.get(i);
            if (aux != null && aux.comparator(ID, finger))
                return aux;
        }
        return ID;
    }

    public byte[] handleBackupRequest() {
        return "OK".getBytes();
    }

    public byte[] handleBackupNhRequest() {
        return "OK".getBytes();
    }

    public byte[] handleRestoreRequest() {
        return "OK".getBytes();
    }

    public byte[] handleDeleteRequest() {
        return "OK".getBytes();
    }

    public byte[] handleDeleteNhRequest() {
        return "OK".getBytes();
    }

    public byte[] handleReclaimRequest() {
        return "OK".getBytes();
    }

    public byte[] handleStateRequest() {
        return "OK".getBytes();
    }
}