package source;

import java.util.Map;

public class Chord {

    private final String address;
    private final int port;
    private Map<Integer, Finger> fingers; // think about that
    private Finger successor;
    private Finger predecessor;

    public Chord(String address, String port) {
        this.address = address;
        this.port = Integer.valueOf(port);

        System.out.println("Chord:\n - Address -> " + address + "\n - Port -> " + port);
    }

    
}