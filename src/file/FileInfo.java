package file;


public class FileInfo {

    private String fileId;
    private long fileSize;
    private int replication;
    private String path;

    public FileInfo(String fileId, long fileSize, int replication, String path){
        this.fileId = fileId;
        this.fileSize = fileSize;
        this.replication = replication;
        this.path = path;
    }

    public String getFileId() {
        return fileId;
    }


    public long getFileSize() {
        return fileSize;
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

        if(this.fileSize != fileInfo.getFileSize()) {
            return false;
        }else{
            if(this.fileId != fileInfo.getFileId())
                return false;
        }

        return true;
    }
}
