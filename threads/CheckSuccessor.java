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
            System.out.println("Successor's not set for " + chordNode.getKey().getID());
            return;
        }

        if (successor.equals(chordNode.getKey())) {
            System.out.println("My successor is myself!");
            return;
        }

        // resquests for the predecessor of my successor
        byte[] request = MessageManager.createHeader(MessageManager.Type.PREDECESSOR, chordNode.getKey().getID(), null);
        byte[] response = RequestManager.sendRequest(successor.getAddress(), successor.getPort(), request);

        String[] splited = MessageManager.parseResponse(response);
        // [PREDECESSOR, (ID, Address, Port) | ERROR]

        if (splited[0].equals("ERROR")) {
            System.out.println("Predecessor of my sucessor is not set!");
            return;
        }

        Finger newCandidate = null;

        try {
            newCandidate = new Finger(splited[2], Integer.parseInt(splited[3]));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (newCandidate.comparator(chordNode.getKey(), successor)) {
            chordNode.setSuccessor(newCandidate);
        }

        chordNode.notifySuccessor();
        System.out.println("My successor is " + newCandidate.getID());
    }
}