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

    @Override
    public boolean equals(Object obj) {

        if(this == obj)
            return true;
        else if(obj == null)
            return false;

        ChunkID ck = (ChunkID)obj;

        if(this.fileID != ck.getFileID()) {
            return false;
        }else{
            if(this.chunkID != ck.getChunkID())
                return false;
        }

        return true;
    }
}
