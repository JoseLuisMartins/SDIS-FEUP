package protocols;


import file.Chunk;
import file.ChunkID;
import file.FileInfo;
import logic.Message;
import logic.MessageType;
import logic.Utils;

import static logic.Utils.sleepSpecificTime;

public class PutChunk implements Runnable{
    private Chunk chunk;
    private int replicationDegree;
    private boolean withEnhancement;

    public PutChunk(Chunk chunk, int replicationDegree,boolean withEnhancement) {
        this.chunk = chunk;
        this.replicationDegree = replicationDegree;
        this.withEnhancement = withEnhancement;
    }

    @Override
    public void run() {
        int time_interval=1000;
        ChunkID chunkId = chunk.getId();

        String version = "1.0";
        if(withEnhancement)
            version = "2.0";

        Boolean backupHasFailed = false;

        for (int j = 0 ; j < Protocol.MAX_PUTCHUNK_TRIES; j++) {//maximum of 5 tries

            Message msg = new Message(MessageType.PUTCHUNK, version, Utils.peerID, chunkId.getFileID(), chunkId.getChunkID(), replicationDegree, chunk.getContent());

            network.Observer obs = new network.Observer(Utils.mc);
            msg.send(Utils.mdb);

            //wait 1 sec
            sleepSpecificTime(time_interval);
            //check responses
            obs.stop();

            if(obs.getMessageNumber(MessageType.STORED,chunkId.getFileID(),chunkId.getChunkID()) >= replicationDegree)
                break;

            //try again
            time_interval*=2;

            if(j == Protocol.MAX_PUTCHUNK_TRIES-1) {
                System.out.println("Exceeded number of putchunk tries");
                //enhancement 4
                backupHasFailed = true;
            }
        }

        //enhancement 4
        FileInfo fileMetadata = Utils.metadata.getFileInfo(chunkId.getFileID());
        if(fileMetadata != null) // only when its a file that i have backed up
            fileMetadata.setBackupHasFailed(backupHasFailed);

    }
}