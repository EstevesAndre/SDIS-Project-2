package threads;

import source.ChordNode;
import source.Finger;

public class CheckFingers implements Runnable {
    private ChordNode chordNode;

    public CheckFingers(ChordNode chordNode) {
        this.chordNode = chordNode;
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
