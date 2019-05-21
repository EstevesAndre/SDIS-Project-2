package threads;

import java.util.Timer;
import java.util.TimerTask;

import source.ChordNode;
import source.Finger;

public class CheckFingers extends Thread {

    private Timer timer;
    private ChordNode chordNode;

    public CheckFingers(ChordNode chordNode, int interval) {
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

        for (int i = 1; i < ChordNode.FINGERS_SIZE; i++) {

            Finger successorFinger = chordNode
                    .findSuccessor(chordNode.getFingerTableIndex(i).getID() + (long) Math.pow(2, i));

            long successorId = successorFinger.getID();

            if (successorId > 0) {
                chordNode.setFingerTableIndex(i, successorFinger);
            }
        }
        System.out.println("Fingers fixed!");
    }
}
