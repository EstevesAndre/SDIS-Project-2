package service;

import source.ChordNode;

public class Peer {
    public static void main(String args[]) throws Exception {
        if (args.length == 2) { // chord first node
            new ChordNode(args[0], args[1]);
        } else if (args.length == 4) { // new chord and existing given
            new ChordNode(args[0], args[1], args[2], args[3]);
        } else {
            System.err.println("Wrong number of arguments.\nUsage: java service/Peer <address_ip>? <address_port>");
            System.exit(-1);
        }
    }
}