package file;


public class Chunk{

    private ChunkID id;
    private byte[] content;

    public Chunk(String fileID , int chunkNo, byte[] content){
        this.id = new ChunkID(fileID, chunkNo);
        this.content = content;

    }

    public ChunkID getId(){
        return id;
    }

    public byte[] getContent(){
        return content;
    }



    public int getSizeOfData(){
        return content.length;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(this.id.getFileID()).append(" - ").append(this.id.getChunkID()).toString();
    }


}
