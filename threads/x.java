package threads;

import java.math.BigInteger;

import source.ChordNode;
import source.Finger;

public class x implements Runnable {
    private ChordNode chordNode;

    public x(ChordNode chordNode) {
        this.chordNode = chordNode;
    }

    @Override
    public void run() {
        if (ChordNode.debug) {
            System.out.println("-----------------------------------------------------------------");
            System.out.format("%5s%15s%24s%7s%15s\n", "Index", "Key", "Looking", "Port", "Successor");
            // for (int i = 0; i < ChordNode.FINGERS_SIZE; i++) {
            // System.out.format("%5s%15s%22s%7d%15s\n", i,
            // chordNode.getKey().getID().toString() + " " +
            // chordNode.getKey().getID().toString().length(),
            // chordNode.getKey().getID().add(new BigInteger("2").pow(i)).toString() + " "
            // + chordNode.getKey().getID().add(new
            // BigInteger("2").pow(i)).toString().length(),
            // chordNode.getFingerTableIndex(i).getPort(),
            // chordNode.getFingerTableIndex(i).getID().toString()
            // + " " + chordNode.getFingerTableIndex(i).getID().toString().length());
            // }

            for (int i = 0; i < ChordNode.FINGERS_SIZE; i++) {
                System.out.format("%5s%15s%24s%7d%15s\n", i,
                        chordNode.getKey().getID().toString().substring(0, 10) + " "
                                + chordNode.getKey().getID().toString().length(),
                        (new Finger(chordNode.getKey().getID().longValue(), i).getID().toString()) + " "
                                + (new Finger(chordNode.getKey().getID().longValue(), i).getID().toString()).length(),
                        chordNode.getFingerTableIndex(i).getPort(),
                        chordNode.getFingerTableIndex(i).getID().toString().substring(0, 10) + " "
                                + chordNode.getFingerTableIndex(i).getID().toString().length());
            }

            System.out.println("-----------------------------------------------------------------");
        }
    }
}
