package protocols;


import file.Chunk;
import file.ChunkID;
import logic.Message;
import logic.MessageType;
import logic.Utils;

import static logic.Utils.sleepSpecificTime;

public class PutChunk implements Runnable{
    private Chunk chunk;
    private int replicationDegree;

    public PutChunk(Chunk chunk, int replicationDegree) {
        this.chunk = chunk;
        this.replicationDegree = replicationDegree;
    }

    @Override
    public void run() {
        int time_interval=1000;
        ChunkID chunkId = chunk.getId();

        for (int j = 0 ; j < Protocol.MAX_PUTCHUNK_TRIES; j++) {//maximum of 5 tries

        Message msg = new Message(MessageType.PUTCHUNK, Utils.version, Utils.peerID, chunkId.getFileID(), chunkId.getChunkID(), replicationDegree, chunk.getContent());

        network.Observer obs = new network.Observer(Utils.mc);
        msg.send(Utils.mdb);

        //wait 1 sec
        sleepSpecificTime(time_interval);
        //check responses
        obs.stop();
        //System.out.println("Number-> " +  obs.getMessageNumber(MessageType.STORED,chunkId.getFileID(),chunkId.getChunkID()) + "\nchunk-> " + chunkId.toString() + "\nj-> " + j);

        if(obs.getMessageNumber(MessageType.STORED,chunkId.getFileID(),chunkId.getChunkID()) >= replicationDegree)
            break;

        //try again
        time_interval*=2;
        }

    }
}
