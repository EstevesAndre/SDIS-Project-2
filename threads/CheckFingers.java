package threads;

import java.util.Timer;
import java.util.TimerTask;

import handlers.MessageManager;
import handlers.RequestManager;
import source.ChordNode;
import source.Finger;

public class CheckFingers extends Thread {

    private Timer timer;
    private ChordNode chordNode;

    CheckFingers(ChordNode chordNode, int interval) {
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
        System.out.println("Fixing fingers...");

        int i = chordNode.nextFingerToFix;

        Finger edit = chordNode
                .findSuccessor(chordNode.getIthFinger(((int) chordNode.getKey().getID() + (int) Math.pow(2, i)) % 32));
        long successorId = edit.getID();

        if (successorId != -1) {
            if (successorId != chordNode.getFingers().get(i).getID()) {
                chordNode.getFingers().put(i, edit);
            }
            chordNode.nextFingerToFix = (i + 1) % 32; // replace with bit_num
        }

    }

    public void terminate() {
        timer.cancel();
    }
}
