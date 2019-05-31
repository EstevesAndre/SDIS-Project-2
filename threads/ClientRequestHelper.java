package threads;

import java.math.BigInteger;

import handlers.Chunk;
import handlers.IOManager;
import handlers.MessageManager;
import handlers.RequestManager;
import service.TestApp;

public class ClientRequestHelper implements Runnable {

    TestApp tp;
    BigInteger chunkKey;
    Chunk chunk;
    int rd, op1;
    String type, file;

    public ClientRequestHelper(String type, TestApp tp, String file, BigInteger chunkKey, int op1, int rd,
            Chunk chunk) {
        this.tp = tp;
        this.chunkKey = chunkKey;
        this.chunk = chunk;
        this.rd = rd;
        this.file = file;
        this.type = type;
        this.op1 = op1;
    }

    @Override
    public void run() {

        switch (type) {
        case "BACKUP":
            byte[] header = MessageManager.createApplicationHeader(MessageManager.Type.BACKUP, file, chunkKey,
                    chunk.getId(), rd);
            byte[] putChunk = new byte[header.length + chunk.getSize()];
            System.arraycopy(header, 0, putChunk, 0, header.length);
            System.arraycopy(chunk.getContent(), 0, putChunk, header.length, chunk.getSize());
            tp.handleResponse(type,
                    RequestManager.sendRequest(tp.getAddress(), Integer.parseInt(tp.getPort()), putChunk));
            break;
        case "FILE_INFO":
            System.out.println(chunkKey);
            byte[] fileInfo = MessageManager.createApplicationHeader(MessageManager.Type.FILE_INFO, null, chunkKey, op1,
                    rd);
            tp.handleResponse(type,
                    RequestManager.sendRequest(tp.getAddress(), Integer.parseInt(tp.getPort()), fileInfo));
        }
    }
}
