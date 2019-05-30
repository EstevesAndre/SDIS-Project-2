package handlers;

public class Chunk {

    private int id;
    private int size;
    private byte[] content; // restore

    public Chunk(int id, byte[] content, int size) {
        this.id = id;
        this.content = content;
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public byte[] getContent() {
        return this.content;
    }

    public int getId() {
        return this.id;
    }

}