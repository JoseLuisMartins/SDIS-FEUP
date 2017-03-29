package logic;



import file.ChunkID;
import file.FileInfo;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;


public class Metadata implements Serializable{
    private HashMap<String, HashSet<Integer>> storedChunksPerceivedDegree;
    private HashMap<String, Integer> storedChunksDesiredDegree;
    private HashMap<String, FileInfo> filesMetadata;
    private int maximumDiskSpace;


    public Metadata() {
        storedChunksPerceivedDegree = new HashMap<>();
        storedChunksDesiredDegree = new HashMap<>();
        filesMetadata = new HashMap<>();

        maximumDiskSpace = 64000;
    }

    public HashMap<String, HashSet<Integer>> getStoredChunksPerceivedDegree() {
        return storedChunksPerceivedDegree;
    }

    public int getPerceivedDegree(ChunkID chunkid) {
        return storedChunksPerceivedDegree.get(chunkid.toString()).size();
    }

    public HashMap<String, FileInfo> getFilesMetadata() {
        return filesMetadata;
    }

    public void addFile(String id, FileInfo info){
        filesMetadata.put(id,info);
    }

    public void removefile(String id){
        filesMetadata.remove(id);
    }

    public int getDesiredDegree(ChunkID chunkid) {
        return storedChunksDesiredDegree.get(chunkid.toString());
    }

    public int getMaximumDiskSpace() {
        return maximumDiskSpace;
    }

    public void setMaximumDiskSpace(int maximumDiskSpace) {
        this.maximumDiskSpace = maximumDiskSpace;
    }

    public void addChunk(ChunkID chunkId,int desiredRepDeg){
        HashSet<Integer> set = new HashSet<>();
        set.add(Utils.peerID);//add my id to the metadata
        storedChunksPerceivedDegree.put(chunkId.toString(),set);
        storedChunksDesiredDegree.put(chunkId.toString(),desiredRepDeg);
    }

    public void updateReplicationDegree(ChunkID chunkId,int serverId,boolean add) {
        HashSet<Integer> set = storedChunksPerceivedDegree.get(chunkId.toString());
        if(add)
            set.add(serverId);
        else
            set.remove(serverId);

        storedChunksPerceivedDegree.put(chunkId.toString(), set);
    }

    public ArrayList getSortedChunksToEliminate(){
        ArrayList<ChunkState> res = new ArrayList<>();

        for(HashMap.Entry<String, HashSet<Integer>> entry : storedChunksPerceivedDegree.entrySet()) {
            String key = entry.getKey();
            int perceivedRep = entry.getValue().size();
            int desiredRep = storedChunksDesiredDegree.get(key);

            int dif = perceivedRep-desiredRep;


            String[] messageFields = key.split("/");
            ChunkID chunkID = new ChunkID(messageFields[0],Integer.parseInt(messageFields[1]));
            res.add(new ChunkState(chunkID,dif));
        }

        //sort descending order - more deference more need of that chunk

        Collections.sort(res);
        Collections.reverse(res);



        return res;
    }


    @Override
    public String toString() {
        String res="-----------------------Metadata-----------------------\n";

        for(HashMap.Entry<String, HashSet<Integer>> entry : storedChunksPerceivedDegree.entrySet()) {
            String key = entry.getKey();
            HashSet<Integer> perceivedRep = entry.getValue();

            res+= "key-> " + key + " Current_Rep_Deg-> " + perceivedRep.size() + " Desired_Rep_Deg-> " + storedChunksDesiredDegree.get(key) + "\n";
            res+= "Server's hosting the chunk-> ";

            for (Integer id: perceivedRep){
                res += id + " / ";
            }
            res += "\n\n";
        }

            res+= "\n  maximumDiskSpace=" + maximumDiskSpace;
        return res;
    }
}
