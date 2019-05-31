package threads;

import java.math.BigInteger;

import handlers.Chunk;
import handlers.MessageManager;
import handlers.RequestManager;
import service.TestApp;
import source.ChordNode;
import source.Finger;

public class SendDeleteChunk implements Runnable {

    ChordNode node;
    BigInteger chunkKey;

    public SendDeleteChunk(ChordNode node, BigInteger chunkKey) {
        this.node = node;
        this.chunkKey = chunkKey;
    }

    @Override
    public void run() {
        Finger chunkSuccessor = node.findSuccessor(chunkKey);

        if (chunkSuccessor == null) {
            System.err.println("No successor for chunk");
            return;
        }

        System.out.println("Chunk successor " + chunkSuccessor);

        byte[] chunkDeleteRequest = MessageManager.createApplicationHeader(MessageManager.Type.DELETE_CHUNK, null,
                chunkKey, 0, 0);
        byte[] chunkDeleteResponse = RequestManager.sendRequest(chunkSuccessor.getAddress(), chunkSuccessor.getPort(),
                chunkDeleteRequest);

        if (chunkDeleteResponse == null) {
            System.err.println("Failed to Delete chunk");
            return;
        }
    }
}
