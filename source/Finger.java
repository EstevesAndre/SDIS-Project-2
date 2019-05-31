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
        this.ID = new BigInteger(String.valueOf(ID.longValue()));
    }

    public Finger(String address, String port) throws Exception {
        this(address, Integer.parseInt(port));
    }

    public Finger(long key, int shift) {
        long newKey = key + (long) Math.pow(2, shift);
        if (newKey < 0)
            newKey += Long.MAX_VALUE;

        this.ID = new BigInteger(String.valueOf(newKey));
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
        boolean x;
        if (left.getID().compareTo(right.getID()) == -1) { // left < right
            // System.out.print("a ");
            x = (ID.compareTo(left.getID()) == 1) && (ID.compareTo(right.getID()) != 1);
        } else {
            // System.out.print("b ");

            x = (ID.compareTo(left.getID()) == 1) || (ID.compareTo(right.getID()) != 1);
        }

        // System.out.println(left.getID() + " " + this.ID + " " + right.getID() + " " +
        // x);
        return x;

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