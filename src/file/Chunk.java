package file;


public class Chunk{

    private int id;
    private byte[] content;
    private int replication;

    public Chunk(int id, byte[] content, int replication){
        this.id = id;
        this.content = content;
        this.replication = replication;
    }

    public int getId(){
        return id;
    }

    public byte[] getContent(){
        return content;
    }

    public int getReplication() {
        return replication;
    }

}
