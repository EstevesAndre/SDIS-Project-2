package source;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import threads.CheckFingers;
import threads.CheckPredecessor;
import threads.CheckSuccessor;
import threads.Listener;
import threads.x;
import handlers.Chunk;
import handlers.IOManager;
import handlers.MessageManager;
import handlers.RequestManager;
import javafx.util.Pair;

public class ChordNode {

    public static final boolean debug = false;
    public static final boolean debug2 = true;

    /**
     * Size of finger table
     */
    public static final int FINGERS_SIZE = 180;

    /**
     * Unique identifier
     */
    private final Finger key;

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
     * Thread executor
     */
    private ScheduledThreadPoolExecutor executor;

    // KEY <Replication Degree used , Number of Chunks>
    private ConcurrentHashMap<BigInteger, Pair<Integer, Integer>> filesInfo;

    private ConcurrentHashMap<AbstractMap.SimpleEntry<BigInteger, Integer>, Chunk> storedChunks;

    /**
     * Constructor of the first chord node to enter the ring (starting node)
     *
     * @param address Address of the machine
     * @param port    Port to comunicate
     * @throws Exception
     */
    public ChordNode(String address, String port) throws Exception {
        this.key = new Finger(address, Integer.parseInt(port));
        existingNodeAddress = null;
        if (ChordNode.debug)
            System.out.println("Creating a new ring\nChord node:\n - Address -> " + this.key.getAddress()
                    + "\n - Port -> " + this.key.getPort());

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
        this.key = new Finger(address, Integer.parseInt(port));
        this.existingNodeAddress = existingAddress;
        this.existingNodePort = Integer.valueOf(existingPort);

        if (this.key.getAddress().equals(this.existingNodeAddress) && this.key.getPort() == this.existingNodePort)
            throw new IllegalArgumentException("Existing ID is equal to new chord node... Attemp failed!");

        if (ChordNode.debug)
            System.out.println("Creating a new Chord node:\n - Address -> " + this.key.getAddress() + "\n - Port -> "
                    + this.key.getPort());
        if (ChordNode.debug)
            System.out.println("Known Chord node:\n - Address -> " + this.existingNodeAddress + "\n - Port -> "
                    + this.existingNodePort);

        this.initialize();
    }

    /**
     * Initial function to start up the class Creates finger table - Initiate system
     * configs - Initiate sucessors - Initiate and Start threads
     *
     * @throws Exception
     */
    private void initialize() throws Exception {
        this.executor = new ScheduledThreadPoolExecutor(4);

        filesInfo = new ConcurrentHashMap<BigInteger, Pair<Integer, Integer>>();
        storedChunks = new ConcurrentHashMap<AbstractMap.SimpleEntry<BigInteger, Integer>, Chunk>();
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
        return this.successor;
    }

    /**
     * Sets the successor
     *
     * @param newSuccessor Next Successor
     */
    public void setSuccessor(Finger newSuccessor) {
        if (newSuccessor == null)
            try {
                this.successor = new Finger(this.getAddress(), this.getPort());
            } catch (Exception e) {
                e.printStackTrace();
            }
        else
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
        if (newPredecessor == null)
            try {
                this.predecessor = new Finger(this.getAddress(), this.getPort());
            } catch (Exception e) {
                e.printStackTrace();
            }
        else
            this.predecessor = newPredecessor;
    }

    /**
     *
     * @return (Finger) Unique Identifier
     */
    public Finger getKey() {
        return this.key;
    }

    /**
     *
     * @return Unique Identifier address
     */
    public String getAddress() {
        return this.key.getAddress();
    }

    /**
     *
     * @return Unique Identifier port
     */
    public int getPort() {
        return this.key.getPort();
    }

    public Finger getFingerTableIndex(int i) {
        return fingers.get(i);
    }

    public HashMap<Integer, Finger> getFingers() {
        return fingers;
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

        if (this.existingNodeAddress == null) { // first node
            for (int i = 0; i < FINGERS_SIZE; i++)
                this.fingers.put(i, new Finger(this.getAddress(), this.getPort()));

        } else {
            // join node to ring
            if (ChordNode.debug)
                System.out.println("Joining node " + this.existingNodeAddress);
            if (ChordNode.debug)
                System.out.println("Finding successor...");

            byte[] request = MessageManager.createHeader(MessageManager.Type.SUCCESSOR, this.key.getID(),
                    new String[] { this.getAddress(), String.valueOf(this.getPort()) });

            byte[] response = RequestManager.sendRequest(this.existingNodeAddress, this.existingNodePort, request);
            // [SUCCESSOR ID ADDRESS PORT]

            if (response == null) {
                if (ChordNode.debug)
                    System.out.println("X Successor is now dead!");
                return;
            }

            String[] parts = MessageManager.parseResponse(response);

            if (parts[0] == "ERROR")
                throw new IllegalArgumentException("Received error");

            System.out.println("Received " + new String(response));

            this.fingers.put(0, new Finger(parts[2], parts[3]));
            for (int i = 1; i < FINGERS_SIZE; i++) {
                this.fingers.put(i, new Finger(this.getAddress(), this.getPort()));
            }
        }

        if (ChordNode.debug)
            System.out.println("Finger table created");
    }

    /**
     * Sets the successor and predecessor
     * 
     * @throws Exception
     */
    private void initializeSuccessors() throws Exception {
        this.successor = this.fingers.get(0); // first
        this.predecessor = new Finger(this.getAddress(), this.getPort());

        if (this.existingNodeAddress == null)
            this.notifySuccessor();

        if (ChordNode.debug)
            System.out.println(this.key.getID() + " " + this.successor.getID() + " " + this.predecessor.getID());
    }

    /**
     * initialize threads, Listener, CheckPredecessor and Successor
     */
    private void initializeThreads() {
        executor.submit(new Listener(this));
        executor.scheduleAtFixedRate(new CheckPredecessor(this), 0, 5, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(new CheckSuccessor(this), 0, 2, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(new CheckFingers(this), 0, 20, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(new x(this), 1, 10, TimeUnit.SECONDS);
    }

    /**
     * Notify sucessor that this node is his predecessor
     */
    public void notifySuccessor() {
        if (this.getSuccessor().equals(this.key))
            return;

        byte[] request = MessageManager.createHeader(MessageManager.Type.YOUR_PREDECESSOR, this.getKey().getID(),
                new String[] { this.getAddress(), String.valueOf(this.getPort()) });

        byte[] response = RequestManager.sendRequest(this.getSuccessor().getAddress(), this.getSuccessor().getPort(),
                request);

        if (response == null)
            return;

        if (!"OK".equals(new String(response)))
            if (ChordNode.debug)
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
            return MessageManager.createHeader(MessageManager.Type.ERROR, null, null);
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

        Finger potential = null;
        try {
            potential = new Finger(received[2], received[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (this.predecessor == null) {
            if (ChordNode.debug)
                System.out.println("(1) My predecessor is now " + potential.getID());
            this.setPredecessor(potential);
        } else if (potential.comparator(predecessor, this.key)) {
            if (ChordNode.debug)
                System.out.println("(2) My predecessor is now " + potential.getID());
            this.setPredecessor(potential);
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
    public Finger findSuccessor(Finger finger) {
        // if given finger is between this ID and his sucessor
        if (successor == null)
            return this.key;

        if (finger.comparator(this.key, successor))
            return successor;

        Finger toSendSuccessorRequest = null;

        for (int i = fingers.size() - 1; i > 0; i--) {
            Finger aux = fingers.get(i);
            if (aux != null && aux.comparator(this.key, finger)) {
                toSendSuccessorRequest = aux;
                break;
            }
        }

        if (toSendSuccessorRequest == null)
            return this.key;

        byte[] request = MessageManager.createHeader(MessageManager.Type.SUCCESSOR, this.key.getID(),
                new String[] { this.getAddress(), String.valueOf(this.getPort()) });
        byte[] response = RequestManager.sendRequest(toSendSuccessorRequest.getAddress(),
                toSendSuccessorRequest.getPort(), request);
        // [SUCCESSOR ID ADDRESS PORT]
        if (response == null) {
            if (ChordNode.debug)
                System.out.println("Y Successor is now dead!");
            return this.key;
        }

        String[] parts = MessageManager.parseResponse(response);

        if (parts[0] == "ERROR")
            throw new IllegalArgumentException("Received error");

        Finger succ = null;
        try {
            succ = new Finger(parts[2], parts[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return succ;
    }

    public Finger findSuccessor(BigInteger key) {
        return findSuccessor(new Finger(key));
    }

    public void setFingerTableIndex(int index, Finger successorFinger) {
        this.fingers.put(index, successorFinger);
    }

    public byte[] handlePutchunkRequest(byte[] content) {

        // store chunk here
        int splitIndex = 0;
        for (int i = 0; i < content.length; i++) {
            if (content[i] == 13) {
                splitIndex = i + 4;
                break;
            }
        }

        byte[] chunkcontent = Arrays.copyOfRange(content, splitIndex, content.length);
        String[] parts = new String(content, 0, splitIndex).trim().split("\\s+");

        // TODO
        // to create key value concurrent hash map
        BigInteger chunkID = new BigInteger(parts[1]);
        int chunkNr = Integer.parseInt(parts[2]);

        this.storeChunk(chunkID, chunkcontent);

        return MessageManager.createHeader(MessageManager.Type.OK, null, null);
    }

    private void storeChunk(BigInteger chunkID, byte[] content) {
        String path = this.getAddress().replace('.', '_') + "/" + this.getPort() + "/";

        // CHANGE
        IOManager.storeChunk(path, chunkID.toString(), content);
    }

    public byte[] handleBackupRequest(byte[] content) {

        // store chunk here
        int splitIndex = 0;
        for (int i = 0; i < content.length; i++) {
            if (content[i] == 13) {
                splitIndex = i + 4;
                break;
            }
        }

        byte[] chunkcontent = Arrays.copyOfRange(content, splitIndex, content.length);

        String[] parts = new String(content, 0, splitIndex).trim().split("\\s+");
        int chunkNr = Integer.parseInt(parts[2]);
        BigInteger chunkID = new BigInteger(parts[1]);

        Finger fileSuccessor = this.findSuccessor(chunkID);

        byte[] saveChunkRequest = MessageManager.createApplicationHeader(MessageManager.Type.PUTCHUNK, null, chunkID,
                chunkNr, 0);

        byte[] putChunk = new byte[saveChunkRequest.length + chunkcontent.length];
        System.arraycopy(saveChunkRequest, 0, putChunk, 0, saveChunkRequest.length);
        System.arraycopy(chunkcontent, 0, putChunk, saveChunkRequest.length, chunkcontent.length);

        byte[] saveChunkResponse = RequestManager.sendRequest(fileSuccessor.getAddress(), fileSuccessor.getPort(),
                putChunk);

        if (saveChunkResponse == null || MessageManager.parseResponse(saveChunkResponse)[0].equals("ERROR")) {
            System.err.println("Chunk not backed up chunk" + chunkNr + " - " + chunkID);
            return MessageManager.createHeader(MessageManager.Type.ERROR, null, null);
        }

        return MessageManager.createApplicationHeader(MessageManager.Type.STORED, null, chunkID, chunkNr, 0);
    }

    public byte[] handleRestoreRequest() {
        return "OK".getBytes();
    }

    public byte[] handleDeleteRequest(String filename) {
        if (ChordNode.debug2)
            System.out.println("DELETE " + filename);

        BigInteger fileHash = IOManager.getStringHashed(filename);
        // requests for the filename owner to get the number of chunks

        Finger fileSuccessor = this.findSuccessor(fileHash);
        if (fileSuccessor == null) {
            System.err.println("File not backed up (1)");
            return MessageManager.createHeader(MessageManager.Type.ERROR, fileHash, null);
        }

        System.out.println("File successor " + fileSuccessor);

        byte[] fileInfoRequest = MessageManager.createApplicationHeader(MessageManager.Type.GET_FILE_INFO, null,
                fileHash, 0, 0);
        byte[] fileInfo = RequestManager.sendRequest(fileSuccessor.getAddress(), fileSuccessor.getPort(),
                fileInfoRequest);

        if (fileInfo == null) {
            System.err.println("Failed to get file information");
            return MessageManager.createHeader(MessageManager.Type.ERROR, fileHash, null);
        }

        String[] fileInfoParts = MessageManager.parseResponse(fileInfo);

        if (fileInfoParts[0].equals("ERROR")) {
            System.err.println("File not backed up (2)");
            return MessageManager.createHeader(MessageManager.Type.ERROR, fileHash, null);
        }

        // SEND request to other nodes to delete file
        for (int i = 0; i < Integer.parseInt(fileInfoParts[1]); i++) {

            BigInteger chunkKey = new BigInteger(fileHash.toString() + "_" + i);

            Finger chunkSuccessor = this.findSuccessor(chunkKey);

            if (chunkSuccessor == null) {
                System.err.println("No successor for chunk" + i);
                return MessageManager.createHeader(MessageManager.Type.ERROR, fileHash, null);
            }

            System.out.println("Chunk" + i + " successor " + chunkSuccessor);

            byte[] chunkDeleteRequest = MessageManager.createApplicationHeader(MessageManager.Type.DELETE, null,
                    chunkKey, 0, 0);
            byte[] chunkDeleteResponse = RequestManager.sendRequest(chunkSuccessor.getAddress(),
                    chunkSuccessor.getPort(), chunkDeleteRequest);

            if (fileInfo == null) {
                System.err.println("Failed to Delete chunk" + i);
                return MessageManager.createHeader(MessageManager.Type.ERROR, chunkKey, null);
            }

            if (MessageManager.parseResponse(chunkDeleteResponse)[0].equals("ERROR"))
                return MessageManager.createHeader(MessageManager.Type.ERROR, chunkKey, null);

        }

        return MessageManager.createHeader(MessageManager.Type.OK, null, null);
    }

    public byte[] handleReclaimRequest() {
        return "OK".getBytes();
    }

    public byte[] handleStateRequest() {
        return "OK".getBytes();
    }

    public byte[] handleGetFileInfo(String[] received) {
        // GET_FILE_INFO fileHash

        if (received.length != 2) {
            System.err.println("Invalid request!");
            return MessageManager.createHeader(MessageManager.Type.ERROR, null, null);
        }
        BigInteger fileHash = new BigInteger(received[1]);

        if (filesInfo.containsKey(fileHash)) {
            // Integer rd = filesInfo.get(fileHash);
            // return MessageManager.createApplicationHeader(MessageManager.Type.FILE_INFO,
            // null, fileHash, 0, rd);
        } else {
            return MessageManager.createHeader(MessageManager.Type.ERROR, null, null);
        }
        return null;
    }

    public byte[] handleSaveFileInfoRequest(String[] received) {
        // if (filesInfo.put(new BigInteger(received[1]), Integer.parseInt(received[2]))
        // != null) {
        // return MessageManager.createHeader(MessageManager.Type.ERROR, null, null);
        // }

        return MessageManager.createHeader(MessageManager.Type.OK, null, null);
    }
}
