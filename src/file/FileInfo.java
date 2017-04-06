package file;


import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class FileInfo implements Serializable{

    private String fileId;
    private int desiredReplication;
    private HashMap<Integer, HashSet<Integer>> peersWithTheChunks;
    private String path;
    private int nChunks;

    public FileInfo(String fileId, int desiredReplication, String path, int nChunks){
        this.fileId = fileId;
        this.desiredReplication = desiredReplication;
        this.path = path;
        this.nChunks = nChunks;
        this.peersWithTheChunks = new HashMap<>();
        for (int i = 0; i <= nChunks ; i++){
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
