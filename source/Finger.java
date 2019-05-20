package source;

import handlers.IOManager;

public class Finger {

    private String ID;
    private String address;
    private int port;

    public Finger(String address, int port) throws Exception {
        this.address = address;
        this.port = port;
        this.ID = IOManager.getAddressHashID(address + '_' + port);
    }

    public Finger(String address, String port) throws Exception {
        this(address, Integer.parseInt(port));
    }

    public String getID() {
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

        if (left.getID().compareTo(right.getID()) < 0) // left < right
            return ID.compareTo(left.getID()) > 0 // this > left
                    && ID.compareTo(right.getID()) <= 0; // this <= right
        else
            return ID.compareTo(left.getID()) > 0 || ID.compareTo(right.getID()) <= 0;
    }

    /**
     * 
     * @param f2 Finger to be compared
     * @return true if this finger is equal to f2, false otherwise
     */
    public boolean equals(Finger f2) {
        return ID.equals(f2.getID());
    }
}