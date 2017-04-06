package file;


import java.io.Serializable;

public class ChunkID implements Serializable{

    // obtained applying SHA256 to some bit string
    private String fileID;
    // chunk ID
    private int chunkNo;

    public ChunkID(String fileID, int chunkID){
        this.fileID = fileID;
        this.chunkNo = chunkID;
    }

    public ChunkID(String fullChunkId) {
        String[] messageFields = fullChunkId.split("/");
        this.fileID = messageFields[0];
        this.chunkNo = Integer.parseInt(messageFields[1]);
    }

    public int getChunkID() {
        return chunkNo;
    }

    public String getFileID() {
        return fileID;
    }


    @Override
    public boolean equals(Object obj) {

        ChunkID ck = (ChunkID)obj;

        if(fileID.equals(ck.getFileID()) && this.chunkNo == ck.getChunkID())
            return true;

        return false;
    }



    @Override
    public String toString() {
        return new StringBuilder().append(this.fileID).append("/").append(this.chunkNo).toString();
    }
}
