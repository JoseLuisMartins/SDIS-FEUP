package logic;



import file.ChunkID;
import file.FileInfo;

import java.io.FileDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings("serial")
public class Metadata implements Serializable{
    private HashMap<String, HashSet<Integer>> storedChunksPerceivedDegree;
    private HashMap<String, Integer> storedChunksDesiredDegree;
    private HashMap<String, FileInfo> backupFilesMetadata;
    private int maximumDiskSpace;



    public Metadata() {
        storedChunksPerceivedDegree = new HashMap<>();
        storedChunksDesiredDegree = new HashMap<>();
        backupFilesMetadata = new HashMap<>();

        maximumDiskSpace = 5000000;
    }

    public String getFileIdByPathName(String pathName){


        for(HashMap.Entry<String, FileInfo> entry : backupFilesMetadata.entrySet()) {
            String key = entry.getKey();
            FileInfo currFIle =  backupFilesMetadata.get(key);

            if(currFIle.getPath().equals(pathName))
                return key;

        }

        return null;
    }

    public HashMap<String, HashSet<Integer>> getStoredChunksPerceivedDegree() {
        return storedChunksPerceivedDegree;
    }

    public int getPerceivedDegree(ChunkID chunkid) {
        return storedChunksPerceivedDegree.get(chunkid.toString()).size();
    }

    public  FileInfo getFileInfo(String fileId){
        return backupFilesMetadata.get(fileId);
    }

    public HashMap<String, FileInfo> getBackupFilesMetadata() {
        return backupFilesMetadata;
    }

    public void addFile(FileInfo info){
        backupFilesMetadata.put(info.getFileId(),info);
    }



    public void removeFile(String id){
        backupFilesMetadata.remove(id);
    }

    public void removeFileChunks(String fileId){
        ArrayList<String> keysToRemove = new ArrayList<>();

        for(HashMap.Entry<String, HashSet<Integer>> entry : storedChunksPerceivedDegree.entrySet()) {
            String key = entry.getKey();

            String[] messageFields = key.split("/");
            String currFileId = messageFields[0];

            if(currFileId.equals(fileId)){
                keysToRemove.add(key);
            }
        }

        for (String key: keysToRemove) {
            storedChunksPerceivedDegree.remove(key);
            storedChunksDesiredDegree.remove(key);
        }

    }

    public Integer getDesiredDegree(ChunkID chunkid) {
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
        if(storedChunksPerceivedDegree.get(chunkId.toString()) != null) {//

            HashSet<Integer> set = storedChunksPerceivedDegree.get(chunkId.toString());
            if (add)
                set.add(serverId);
            else
                set.remove(serverId);

            storedChunksPerceivedDegree.put(chunkId.toString(), set);
        }else {//it's a chunk that i have backed up
            FileInfo file = backupFilesMetadata.get(chunkId.getFileID());
            file.updateFileChunk(chunkId.getChunkID(),serverId,add);
        }


    }


    public void removeChunk(ChunkID chunkId) {
        storedChunksPerceivedDegree.remove(chunkId.toString());
        storedChunksDesiredDegree.remove(chunkId.toString());
    }

    public ArrayList<ChunkState> getSortedChunksToEliminate(){
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


        Collections.sort(res);


        String debug = "delete chunk-> \n";
        for(int i=0; i < res.size();i++){
            debug += res.get(i).toString();
        }

        System.out.println(debug);


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

        res+= "\n  maximumDiskSpace=" + maximumDiskSpace + "\n";


        for(HashMap.Entry<String, FileInfo> entry : backupFilesMetadata.entrySet()) {
            String key = entry.getKey();

            res += backupFilesMetadata.get(key).toString();
        }


        return res;
    }
}
