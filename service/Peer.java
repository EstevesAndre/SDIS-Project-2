package service;

import source.ChordNode;

public class Peer {

    public static void main(String args[]) throws Exception {
        if (args.length == 0) { // test
            new ChordNode("127.0.0.1", "9876"); // <my pc ip> <port>
        } else if (args.length == 1) { // new chord test
            new ChordNode("127.0.0.1", args[0], "127.0.0.1", "9876"); // <my pc ip> <port> <existing ip> <existing port>
        } else if (args.length == 1) { // chord first node
            new ChordNode("127.0.0.1", args[0]);
        } else if (args.length == 3) { // new chord and existing given
            new ChordNode("127.0.0.1", args[0], args[1], args[2]);
        } else {
            System.err.println("Wrong number of arguments.\nUsage: java service/Peer <address_ip>? <address_port>");
            System.exit(-1);
        }
    }
}