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

    public String getID() {
        return ID;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}