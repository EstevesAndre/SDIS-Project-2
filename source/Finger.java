package source;

import java.math.BigInteger;
import handlers.IOManager;

public class Finger {

    private BigInteger ID;
    private String address;
    private int port;

    public Finger(String address, int port) throws Exception {
        this.address = address;
        this.port = port;
        this.ID = IOManager.getStringHashed(address + '_' + port);
    }

    public Finger(String address, String port) throws Exception {
        this(address, Integer.parseInt(port));
    }

    public Finger(BigInteger key) {
        this.ID = key;
    }

    public BigInteger getID() {
        return ID;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    /**
     * 
     * @param left  Left Finger to compare
     * @param right Right Finger to compare
     * @return true if finger is betweem left and right, false otherwise
     */
    public boolean comparator(Finger left, Finger right) {

        if (left.getID().compareTo(right.getID()) == -1) { // left < right
            return (ID.compareTo(left.getID()) == 1) && (ID.compareTo(right.getID()) != 1);
        } else {
            return (ID.compareTo(left.getID()) == 1) || (ID.compareTo(right.getID()) != 1);
        }
    }

    /**
     * 
     * @param f2 Finger to be compared
     * @return true if this finger is equal to f2, false otherwise
     */
    public boolean equals(Finger f2) {
        return ID.compareTo(f2.getID()) == 0;
    }

    public String toString() {
        return address + " " + port;
    }
}