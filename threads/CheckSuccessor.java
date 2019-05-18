package threads;

import java.util.Timer;
import java.util.TimerTask;

import handlers.MessageManager;
import handlers.RequestManager;
import source.ChordNode;
import source.Finger;

public class CheckSuccessor extends Thread {

    private Timer timer;
    private ChordNode chordNode;

    public CheckSuccessor(ChordNode chordNode, int interval) {
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
        Finger successor = chordNode.getSuccessor();

        if (successor == null) {
            System.out.println("Successor's not set for " + chordNode.getID());
            return;
        }

        byte[] request = MessageManager.createHeader(MessageManager.Type.PREDECESSOR, chordNode.getAddress(), null);
        byte[] response = RequestManager.sendRequest(successor, request);

        // String key = MessageManager.parseResponse(response)[1];

        // if (response == null)
        // chordNode.setPredecessor(null);
        // else if (key.equals(successor.getID()))

        System.out.println("Successor");
    }

    public void terminate() {
        timer.cancel();
    }
}