package file;


public class FileInfo {

    private String fileId;
    private long fileSize;

    public FileInfo(String fileId, long fileSize){
        this.fileId = fileId;
        this.fileSize = fileSize;
    }

    public String getFileId() {
        return fileId;
    }


    public long getFileSize() {
        return fileSize;
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
