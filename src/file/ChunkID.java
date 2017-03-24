package file;


public class ChunkID {

    // obtained applying SHA256 to some bit string
    private String fileID;
    // chunk ID
    private int chunkNo;

    public ChunkID(String fileID, int chunkID){
        this.fileID = fileID;
        this.chunkNo = chunkID;
    }

    public int getChunkID() {
        return chunkNo;
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
            if(this.chunkNo != ck.getChunkID())
                return false;
        }

        return true;
    }


    @Override
    public String toString() {
        return new StringBuilder().append(this.fileID).append("/").append(this.chunkNo).toString();
    }
}
