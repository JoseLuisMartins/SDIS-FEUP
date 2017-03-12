package file;


public class ChunkID {

    // obtained applying SHA256 to some bit string
    private String fileID;

    // chunk ID
    private int chunkID;

    public ChunkID(String fileID, int chunkID){
        this.fileID = fileID;
        this.chunkID = chunkID;
    }

    public int getChunkID() {
        return chunkID;
    }

    public String getFileID() {
        return fileID;
    }

}
