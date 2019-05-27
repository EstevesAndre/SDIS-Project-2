package handlers;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.AbstractMap;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;

import handlers.Chunk;

public class IOManager implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private static final int MAX_CHUNK_SIZE = 64000;

    private String fileID;
    private String path;
    private int rd;
    private int chunkNr;
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public IOManager(String fileID, String path, int rd) {
        this.fileID = fileID;
        this.path = path;
        this.rd = rd;
    }

    public IOManager(String fileID, String path, int rd, int chunkNr) {
        this.fileID = fileID;
        this.path = path;
        this.rd = rd;
        this.chunkNr = chunkNr;
    }

    public String getFileID() {
        return this.fileID;
    }

    public String getPath() {
        return path;
    }

    public int getDRD() {
        return rd;
    }

    public int getChunkNr() {
        return chunkNr;
    }

    public void setRD(int newRD) {
        this.rd = newRD;
    }

    public static ArrayList<Chunk> splitFile(String fileID, String path, int rd) {

        System.out.println(path);
        int partCounter = 0;
        ArrayList<Chunk> chunks = new ArrayList<Chunk>();

        try {
            FileInputStream fin = new FileInputStream(path);
            FileChannel fc = fin.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(MAX_CHUNK_SIZE);
            int bytesAmount = 0;

            while ((bytesAmount = fc.read(buffer)) > 0) {
                buffer.flip();
                byte[] arr = new byte[bytesAmount];
                for (int i = 0; i < buffer.limit(); i++) {
                    arr[i] = buffer.get();
                }
                buffer.clear();

                chunks.add(new Chunk(fileID, partCounter, arr, bytesAmount, rd));
                partCounter++;
            }
            fc.close();
            fin.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return chunks;
    }

    public static void restoreFile(String path, String fileID, int nrChunks,
            ConcurrentHashMap<AbstractMap.SimpleEntry<String, Integer>, byte[]> chunks) throws IOException {

        try {
            FileOutputStream fout = new FileOutputStream("x" + path);
            FileChannel fc = fout.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(MAX_CHUNK_SIZE);

            for (int chunkID = 0; chunkID < nrChunks; chunkID++) {
                for (AbstractMap.SimpleEntry<String, Integer> key : chunks.keySet()) {
                    if (key.getKey().equals(fileID) && key.getValue() == chunkID) {
                        buffer.put(chunks.get(key));
                        buffer.flip();
                        fc.write(buffer);
                        buffer.clear();
                    }
                }
            }

            fout.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            System.err.println("Error while restoring File\n");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while restoring File\n");
        }
    }

    public static void storeChunk(String pathString, String filename, byte[] content) {

        try {
            Path path = Paths.get(pathString);

            if (!Files.exists(path))
                Files.createDirectories(path);

            FileOutputStream fout;
            fout = new FileOutputStream(pathString + '/' + filename);

            FileChannel fc = fout.getChannel();

            ByteBuffer buffer = ByteBuffer.allocate(MAX_CHUNK_SIZE);

            for (int i = 0; i < content.length; ++i) {
                buffer.put(content[i]);
            }

            buffer.flip();

            fc.write(buffer);

            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BigInteger getStringHashed(String toHash) {
        MessageDigest digest;
        byte[] hashed = null;

        try {
            digest = MessageDigest.getInstance("SHA-1");
            hashed = digest.digest(toHash.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("Error: SHA-1 Failed!");
            e.printStackTrace();
        }

        return new BigInteger(1, hashed);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String getFileHashID(String path) {
        File file = new File(path);
        Path p = Paths.get(file.getAbsolutePath());

        try {
            BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);

            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            String toHash = file.getName();

            return bytesToHex(digest.digest(toHash.getBytes(StandardCharsets.UTF_8)));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void deleteChunk(String path) {

        try {
            Path filePath = Paths.get(path);
            if (Files.exists(filePath))
                Files.delete(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}