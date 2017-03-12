package file;


public class ChunkId {

    private int fileId;
    private int chunkId;

    public ChunkId(int fileId, int chunkId){
        this.fileId = fileId;
        this.chunkId = chunkId;
    }

    public int getChunkId() {
        return chunkId;
    }

    public int getFileId() {
        return fileId;
    }
}
