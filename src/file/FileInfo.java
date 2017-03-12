package file;


public class FileInfo {

    private String fileName;
    private int fileId;
    private long fileSize;

    public FileInfo(String fileName, int fileId, long fileSize){
        this.fileName = fileName;
        this.fileId = fileId;
        this.fileSize = fileSize;
    }

    public int getFileId() {
        return fileId;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileName() {
        return fileName;
    }
}
