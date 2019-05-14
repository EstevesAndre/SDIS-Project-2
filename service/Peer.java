package service;

import source.ChordNode;

public class Peer {

    // Example: java project/service/TestApp "localhost RemoteInterface2" RECLAIM 0
    public static void main(String args[]) throws Exception {

        if (args.length == 0) {
            new ChordNode("230.0.0.0", "9876");
        } else if (args.length == 1) {
            new ChordNode("230.0.0.0", args[0]);
        } else if (args.length == 2) {
            new ChordNode(args[0], args[1]);
        } else {
            System.err.println("Wrong number of arguments.\nUsage: java service/TestApp <address_ip>? <address_port>");
            System.exit(-1);
        }
    }

}