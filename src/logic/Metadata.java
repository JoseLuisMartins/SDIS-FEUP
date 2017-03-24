package logic;

import file.ChunkID;


import java.io.Serializable;
import java.util.HashMap;

public class Metadata implements Serializable{
    private HashMap<ChunkID, Integer> chunksMetadata;
    private int maximumDiskSpace;


    public Metadata() {
        chunksMetadata = new HashMap<>();
        maximumDiskSpace = 64000;
    }

    public HashMap<ChunkID, Integer> getChunksMetadata() {
        return chunksMetadata;
    }

    public int getMaximumDiskSpace() {
        return maximumDiskSpace;
    }

    public void setMaximumDiskSpace(int maximumDiskSpace) {
        this.maximumDiskSpace = maximumDiskSpace;
    }

    public void incReplicationDegree(ChunkID chunkId) {
        Integer currDegree = chunksMetadata.get(chunkId);

        if(currDegree == null)
            chunksMetadata.put(chunkId,1);
        else
            chunksMetadata.put(chunkId, currDegree + 1);
    }


    @Override
    public String toString() {
        return "Metadata{" +
                "chunksMetadata=" + chunksMetadata +
                ", maximumDiskSpace=" + maximumDiskSpace +
                '}';
    }
}
