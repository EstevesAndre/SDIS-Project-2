package threads;

import java.nio.charset.StandardCharsets;

import handlers.MessageManager;
import handlers.RequestManager;
import source.ChordNode;
import source.Finger;

public class CheckSuccessor implements Runnable {
    private ChordNode chordNode;

    public CheckSuccessor(ChordNode chordNode) {
        this.chordNode = chordNode;
    }

    @Override
    public void run() {
        while (true) {
            Finger successor = chordNode.getSuccessor();

            if (successor == null) {
                if (ChordNode.debug)
                    System.out.println("Successor's not set for " + chordNode.getKey().getID());
                return;
            }
            // resquests for the predecessor of my successor
            byte[] request = MessageManager.createHeader(MessageManager.Type.PREDECESSOR, chordNode.getKey().getID(),
                    null);
            byte[] response = null;

            if (chordNode.getPredecessor().equals(chordNode.getKey())) {
                response = chordNode
                        .handlePredecessorRequest(new String(request, StandardCharsets.UTF_8).trim().split("\\s+"));
            } else
                response = RequestManager.sendRequest(successor.getAddress(), successor.getPort(), request);

            if (response == null) {
                if (ChordNode.debug)
                    System.out.println("Z Successor is now dead!");
                chordNode.setSuccessor(null);
                return;
            }

            String[] splited = MessageManager.parseResponse(response);
            // [PREDECESSOR, (ID, Address, Port) | ERROR

            if (splited[0].equals("ERROR")) {
                if (ChordNode.debug)
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

            if (newCandidate.comparator(chordNode.getKey(), successor)
                    || chordNode.getKey().equals(chordNode.getSuccessor())) {
                chordNode.setSuccessor(newCandidate);
            }

            chordNode.notifySuccessor();
            if (ChordNode.debug)
                if (chordNode.getSuccessor().equals(chordNode.getKey()))
                    System.out.println("SUCCESSOR " + chordNode.getSuccessor().getID() + " (ME)");
                else
                    System.out.println("SUCCESSOR " + chordNode.getSuccessor().getID());

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}