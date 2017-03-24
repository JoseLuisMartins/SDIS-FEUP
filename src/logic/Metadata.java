package logic;

import file.ChunkID;


import java.util.HashMap;

public class Metadata{
    private HashMap<ChunkID, Integer> chunksMetadata;
    private int maximumDiskSpace;


    public Metadata() {
        chunksMetadata = new HashMap<>();
    }

    public HashMap<ChunkID, Integer> getChunksMetadata() {
        return chunksMetadata;
    }

    public int getMaximumDiskSpace() {
        return maximumDiskSpace;
    }

    public void incReplicationDegree(ChunkID chunkId) {
        Integer currDegree = chunksMetadata.get(chunkId);

        if(currDegree == null)
            chunksMetadata.put(chunkId,1);
        else
            chunksMetadata.put(chunkId, currDegree + 1);
    }



}
