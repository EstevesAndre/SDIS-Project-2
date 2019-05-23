package threads;

import java.math.BigInteger;
import java.util.Timer;
import java.util.TimerTask;

import handlers.MessageManager;
import handlers.RequestManager;
import source.ChordNode;
import source.Finger;

public class CheckPredecessor implements Runnable {
    private ChordNode chordNode;

    public CheckPredecessor(ChordNode chordNode) {
        this.chordNode = chordNode;
    }

    @Override
    public void run() {
        Finger predecessor = chordNode.getPredecessor();

        if (predecessor == null) {
            System.out.println("Predecessor's not set for " + chordNode.getKey().getID());
            return;
        } else if (predecessor.equals(chordNode.getKey())) {
            System.out.println("PREDECESSOR " + chordNode.getKey().getID() + " (ME)");
            return;
        }
        System.out.println("PREDECESSOR " + predecessor.getID());

        byte[] request = MessageManager.createHeader(MessageManager.Type.KEY, chordNode.getKey().getID(), null);
        byte[] response = RequestManager.sendRequest(predecessor.getAddress(), predecessor.getPort(), request);

        if (response == null) {
            System.out.println("Predecessor is now dead!");
            chordNode.setPredecessor(null);
            return;
        }
        String key = MessageManager.parseResponse(response)[1];
        if (predecessor.getID().compareTo(new BigInteger(key)) == 0)
            System.out.println("Predecessor is still alive!!");
        else
            System.err.println("Found predecessor with invalid key");
    }
}