package file;


import java.util.ArrayList;

public class FileInfo {

    private String fileId;
    private int replication;
    private String path;
    private ArrayList<Chunk> chunks;

    public FileInfo(String fileId, int replication, String path, ArrayList<Chunk> chunks){
        this.fileId = fileId;
        this.replication = replication;
        this.path = path;
        this.chunks = chunks;
    }

    public String getFileId() {
        return fileId;
    }

    public int getReplication() {
        return replication;
    }

    public String getPath() {
        return path;
    }

    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    @Override
    public boolean equals(Object obj) {

        if(this == obj)
            return true;
        else if(obj == null)
            return false;

        FileInfo fileInfo = (FileInfo) obj;

        if(this.fileId != fileInfo.getFileId())
            return false;


        return true;
    }
}
