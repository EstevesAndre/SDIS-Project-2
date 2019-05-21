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
        Finger predecessor = chordNode.getPredecessor();

        if (predecessor == null) {
            System.out.println("Predecessor's not set for " + chordNode.getKey().getID());
            return;
        } else if (predecessor.getID() == chordNode.getKey().getID()) {
            System.out.println("I'm my own predecessor, with ID " + chordNode.getKey().getID());
            return;
        }
        System.out.println("My current predecessor is " + predecessor.getID());

        byte[] request = MessageManager.createHeader(MessageManager.Type.KEY, chordNode.getKey().getID(), null);
        byte[] response = RequestManager.sendRequest(predecessor.getAddress(), predecessor.getPort(), request);

        if (response == null) {
            System.out.println("Predecessor is now dead!");
            chordNode.setPredecessor(null);
            return;
        }
        String key = MessageManager.parseResponse(response)[1];
        if (Integer.parseInt(key) == predecessor.getID())
            System.out.println("Predecessor is still alive!!");
        else
            System.err.println("Found predecessor with invalid key");
    }

    public void terminate() {
        timer.cancel();
    }
}