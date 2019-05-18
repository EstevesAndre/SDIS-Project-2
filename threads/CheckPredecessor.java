package threads;

import java.util.Timer;
import java.util.TimerTask;

import handlers.MessageManager;
import handlers.RequestManager;
import source.ChordNode;
import source.Finger;

public class CheckPredecessor extends Thread {

    private Timer timer;
    private ChordNode chordNode;

    public CheckPredecessor(ChordNode chordNode, int interval) {
        this.timer = new Timer();
        this.chordNode = chordNode;

        Runnable r = this;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                r.run();
            }
        }, interval, interval);
    }

    @Override
    public void run() {
        System.out.println("Checking predecessor...");
        Finger predecessor = chordNode.getPredecessor();

        if (predecessor == null) {
            System.out.println("Predecessor's not set for " + chordNode.getID());
            return;
        }

        byte[] request = MessageManager.createHeader(MessageManager.Type.KEY, chordNode.getAddress(), null);
        byte[] response = RequestManager.sendRequest(predecessor, request);
        String key = MessageManager.parseResponse(response)[1];

        if (response == null)
            chordNode.setPredecessor(null);
        else if (key.equals(predecessor.getID()))
            System.out.println("Predecessor is still alive!!");
        else
            System.err.println("Found predecessor with invalid key");

    }

    public void terminate() {
        timer.cancel();
    }
}