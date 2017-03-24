package file;


public class FileInfo {

    private String fileId;
    private int replication;
    private String path;

    public FileInfo(String fileId, int replication, String path){
        this.fileId = fileId;
        this.replication = replication;
        this.path = path;
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
