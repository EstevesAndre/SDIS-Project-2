package source;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.concurrent.ConcurrentHashMap;

public class Storage implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    // KEY <Number of Chunks, Replication Degree used>
    private ConcurrentHashMap<BigInteger, AbstractMap.SimpleEntry<Integer, Integer>> filesInfo;

    // < Chunk's key, Chunk number >
    private ConcurrentHashMap<BigInteger, Integer> storedChunks;

    private long capacityAvailable;
    private long maxCapacity;

    public Storage() {
        filesInfo = new ConcurrentHashMap<BigInteger, AbstractMap.SimpleEntry<Integer, Integer>>();
        storedChunks = new ConcurrentHashMap<>();

        maxCapacity = 10000000;
        capacityAvailable = maxCapacity;
    }

    public ConcurrentHashMap<BigInteger, Integer> getStoredChunks() {
        return storedChunks;
    }

    public ConcurrentHashMap<BigInteger, AbstractMap.SimpleEntry<Integer, Integer>> getFilesInfo() {
        return filesInfo;
    }

    public long getCapacityAvailable() {
        return this.capacityAvailable;
    }

    public long getMaxCapacity() {
        return maxCapacity;
    }

    public void updateCapacityAvailable(long tmh) {
        this.capacityAvailable -= tmh;
    }

}