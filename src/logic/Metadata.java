package logic;


import file.FileInfo;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static logic.Utils.CHUNKS_FOLDER_NAME;
import static management.FileManager.getSizeOfFolder;

public class Metadata implements Serializable{
    private HashMap<String, Integer[]> chunksMetadata;
    private HashMap<String, FileInfo> filesMetadata;

    private int maximumDiskSpace;

    //indexes
    private static int CURRENT_REPLICATION_DEGREE=0;
    private static int DESIRED_REPLICATION_DEGREE=1;
    private static int SIZE = 2;

    public Metadata() {
        chunksMetadata = new HashMap<>();
        filesMetadata = new HashMap<>();
        maximumDiskSpace = 64000;
    }

    public HashMap<String, FileInfo> getFilesMetadata() {
        return filesMetadata;
    }

    public HashMap<String, Integer[]> getChunksMetadata() {
        return chunksMetadata;
    }


    public int getMaximumDiskSpace() {
        return maximumDiskSpace;
    }

    public void setMaximumDiskSpace(int maximumDiskSpace) {
        this.maximumDiskSpace = maximumDiskSpace;
    }


    public void addChunk(String chunkId,int desiredRepDeg, int size){
        Integer[] degrees = new Integer[3];
        degrees[CURRENT_REPLICATION_DEGREE]=1;
        degrees[DESIRED_REPLICATION_DEGREE]=desiredRepDeg;
        degrees[SIZE] = size;
        chunksMetadata.put(chunkId,degrees);
    }

    public void addFile(String path, String fileID, int replication){
        FileInfo info = new FileInfo( fileID, replication, path);

        filesMetadata.put(fileID, info);
    }

    public void removeFile(String fileID){
        filesMetadata.remove(fileID);
    }

    public void updateReplicationDegree(String chunkId,int val) {
        Integer[] currDegree = chunksMetadata.get(chunkId);
        currDegree[CURRENT_REPLICATION_DEGREE] += val;
        chunksMetadata.put(chunkId, currDegree);
    }

    public long getOccupiedDisk(){
        File folder = new File(CHUNKS_FOLDER_NAME);
        return getSizeOfFolder(folder);
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "chunksMetadata=" + chunksMetadata +
                ", maximumDiskSpace=" + maximumDiskSpace +
                '}';
    }

    public void printInfo(){
        /*
           This operation allows to observe the service state. In response to such a request,
           the peer shall send to the client the following information:
           For each file whose backup it has initiated:
             The file pathname
             The backup service id of the file
             The desired replication degree
             For each chunk of the file:
                 Its id
                 Its perceived replication degree

         */
        System.out.println("-------- SERVICE STATE --------");

        Iterator itFiles = Utils.metadata.getFilesMetadata().entrySet().iterator();


        /*  For each File Should print:
                -> pathname
                -> id
                -> replication degree
         */

        while(itFiles.hasNext()){
            Map.Entry pair = (Map.Entry)itFiles.next();

            FileInfo info = (FileInfo)pair.getValue();

            System.out.print("Path: ");
            System.out.println(info.getPath());

            System.out.print("ID: ");
            System.out.println(pair.getKey());

            System.out.print("Replication degree: ");
            System.out.println(info.getReplication());
        }


        Iterator itChunks = Utils.metadata.getChunksMetadata().entrySet().iterator();
        /*
            For each chunk should print:
                - id
                - size
                - replication degree
        */


        System.out.println("---- CHUNKS SAVED-----");
        while (itChunks.hasNext()){
            Map.Entry pair = (Map.Entry)itChunks.next();

            System.out.print("ID: ");
            System.out.println(pair.getKey());

            System.out.print("Size: ");
            System.out.println(pair.getValue());

            System.out.print("Replication degree: ");
            System.out.println(pair.getValue());
        }


        System.out.print("Maximum amount of disk: ");
        System.out.println(Utils.metadata.getMaximumDiskSpace());

    }
}
