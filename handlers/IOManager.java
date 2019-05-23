package handlers;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.AbstractMap;
import java.util.Arrays;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.io.FileOutputStream;

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

    public static ArrayList<Chunk> splitFile(String fileID, String path, int rd) throws IOException {
        File file = new File(path);

        System.out.println(path);
        int partCounter = 0;
        ArrayList<Chunk> chunks = new ArrayList<Chunk>();

        byte[] buffer = new byte[MAX_CHUNK_SIZE];

        // String fileName = file.getName();

        // try-with-resources to ensure closing stream
        try (FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis)) {

            int bytesAmount = 0;
            while ((bytesAmount = bis.read(buffer)) > 0) {
                byte[] copy = Arrays.copyOf(buffer, bytesAmount);

                chunks.add(new Chunk(fileID, partCounter, copy, bytesAmount, rd));
                partCounter++;
            }
        } catch (Exception e) {
            System.err.println("WARNING --> : File: \"" + path + "\" not found!\n");
        }

        return chunks;
    }

    public static void restoreFile(String path, String fileID, int nrChunks,
            ConcurrentHashMap<AbstractMap.SimpleEntry<String, Integer>, byte[]> chunks) throws IOException {

        // System.out.println("PATH = " + path);
        File file = new File(path);

        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(file, true);

            for (int chunkID = 0; chunkID < nrChunks; chunkID++) {
                for (AbstractMap.SimpleEntry<String, Integer> key : chunks.keySet()) {
                    if (key.getKey().equals(fileID) && key.getValue() == chunkID) {
                        byte[] p = chunks.get(key);
                        fos.write(p);
                    }
                }
            }

            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error while restoring File\n");
        }
    }

    public static BigInteger getAddressHashID(String toHash) throws Exception {
        MessageDigest digest;
        byte[] hashed = null;

        try {
            // Create new SHA-1 digest
            digest = MessageDigest.getInstance("SHA-1");
            hashed = digest.digest(toHash.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            System.err.println("Error: SHA-1 Failed!");
            e.printStackTrace();
        }

        return new BigInteger(1, hashed);
    }

    public static String bytesToHex(byte[] bytes) throws Exception {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String getFileHashID(String path) throws Exception {
        File file = new File(path);
        Path p = Paths.get(file.getAbsolutePath());

        BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        String toHash = file.getName() + attr.creationTime().toString() + attr.lastModifiedTime().toString();

        return bytesToHex(digest.digest(toHash.getBytes(StandardCharsets.UTF_8)));
    }
}