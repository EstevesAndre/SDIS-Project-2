package source;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;

public class Chord {

    private static final int FINGERS_SIZE = 32;

    private final String Id;
    private final String address;
    private final int port;
    private HashMap<Integer, Finger> fingers; // think about that
    private Finger successor;
    private Finger predecessor;

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public Chord(String address, String port) throws Exception {
        this.address = address;
        this.Id = getAddressHashID(address + '_' + port);
        this.port = Integer.valueOf(port);

        fingers = new HashMap<Integer, Finger>(FINGERS_SIZE);

        System.out.println("Chord:\n - Address -> " + address + "\n - Port -> " + port);
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

    public String getId() {
        return Id;
    }

    public String getAddress() {
        return address;
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

}