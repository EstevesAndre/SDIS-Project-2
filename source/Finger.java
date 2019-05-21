package source;

import handlers.IOManager;

public class Finger {

    private long ID;
    private String address;
    private int port;

    public Finger(String address, int port) throws Exception {
        this.address = address;
        this.port = port;
        this.ID = IOManager.getAddressHashID(address + '_' + port) % (int) Math.pow(2, 32);
    }

    public Finger(String address, String port) throws Exception {
        this(address, Integer.parseInt(port));
    }

    public long getID() {
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

        if (left.getID() < right.getID()) // left < right
            return this.ID > left.getID() && this.ID <= right.getID(); // this > left && this <= right
        else
            return this.ID > left.getID() || this.ID <= right.getID();
    }

    /**
     * 
     * @param f2 Finger to be compared
     * @return true if this finger is equal to f2, false otherwise
     */
    public boolean equals(Finger f2) {
        return ID == f2.getID();
    }
}