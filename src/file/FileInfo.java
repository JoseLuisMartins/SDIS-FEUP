package file;


import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings("serial")
public class FileInfo implements Serializable{

    private String fileId;
    private int desiredReplication;
    private HashMap<Integer, HashSet<Integer>> peersWithTheChunks;
    private String path;
    private boolean inDeleteProcess;
    private int nChunks;
    //enhancement 4
    private boolean backupHasFailed;

    public FileInfo(String fileId, int desiredReplication, String path, int nChunks){
        this.fileId = fileId;
        this.desiredReplication = desiredReplication;
        this.path = path;
        this.inDeleteProcess =false;
        this.nChunks = nChunks;
        this.peersWithTheChunks = new HashMap<>();
        this.backupHasFailed = false;

        for (int i = 0; i < nChunks ; i++){
            peersWithTheChunks.put(i,new HashSet<>());
        }

    }



    public void updateFileChunk(int chunkNo,int serverId,boolean add){
        HashSet<Integer> set = peersWithTheChunks.get(chunkNo);
        if(add)
            set.add(serverId);
        else
            set.remove(serverId);

        peersWithTheChunks.put(chunkNo, set);
    }

    public int getPerceivedDegree(int chunkNo){
        return peersWithTheChunks.get(chunkNo).size();
    }

    public String getFileId() {
        return fileId;
    }

    public int getDesiredReplication() {
        return desiredReplication;
    }

    public String getPath() {
        return path;
    }

    public int getNChunks() {
        return nChunks;
    }


    public void setDeleted(boolean deleted) {
        this.inDeleteProcess = deleted;
    }

    public boolean isOnDeleteProcess() {
        return inDeleteProcess;
    }

    public void deletePeerChunks(int peerId) {//remove all the chunks of the file stored in the peer wir peerId
        for(HashMap.Entry<Integer, HashSet<Integer>> entry : peersWithTheChunks.entrySet()) {
            Integer key = entry.getKey();

            peersWithTheChunks.get(key).remove(peerId);
        }

    }

    public boolean isFileFullyDeleted() {//check for all chunks if they are not stored in any peer

        for(HashMap.Entry<Integer, HashSet<Integer>> entry : peersWithTheChunks.entrySet()) {
            Integer key = entry.getKey();

            if(peersWithTheChunks.get(key).size() > 0){
                return false;
            }
        }

        return true;

    }

    public boolean getIfBackupHasFailed() {
        return this.backupHasFailed;
    }

    public void setBackupHasFailed(boolean backupHasFailed) {
        this.backupHasFailed = backupHasFailed;
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

    @Override
    public String toString() {
        return "FileInfo{" +
                "fileId='" + fileId + '\'' +
                ", desiredReplication=" + desiredReplication +
                ", peersWithTheChunks=" + peersWithTheChunks +
                ", path='" + path + '\'' +
                ", inDeleteProcess=" + inDeleteProcess +
                ", nChunks=" + nChunks +
                ", backupHasFailed=" + backupHasFailed +
                '}';
    }
}
