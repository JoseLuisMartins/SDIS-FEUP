package network;


import file.Chunk;
import file.ChunkID;
import logic.Message;
import logic.MessageType;
import logic.Utils;
import java.util.ArrayList;

import static logic.Utils.sleepSpecificTime;

public class Protocol {
    public static int MAX_PUTCHUNK_TRIES = 5;


    public static void startBackup(ArrayList<Chunk> chunkList, int replicationDegree){


        for (int i = 0; i < chunkList.size(); i++){//for each chunk
            Chunk currentChunk = chunkList.get(i);
            ChunkID chunkId = chunkList.get(i).getId();
            int time_interval=1000;

            for (int j = 0 ; j < MAX_PUTCHUNK_TRIES; j++) {//maximum of 5 tries


                Message msg = new Message(MessageType.PUTCHUNK, Utils.version, Utils.peerID, chunkId.getFileID(), chunkId.getChunkID(), replicationDegree, currentChunk.getContent());

                Observer obs = new Observer(Utils.mc);
                msg.send(Utils.mdb);

                //wait 1 sec
                sleepSpecificTime(time_interval);
                //check responses
                obs.stop();
                System.out.println("Number-> " +  obs.getPutChunkNumber(MessageType.PUTCHUNK,chunkId.getFileID(),chunkId.getChunkID()) + "\nchunk-> " + chunkId.toString() + "j-> " + j);

                if(obs.getPutChunkNumber(MessageType.STORED,chunkId.getFileID(),chunkId.getChunkID()) >= replicationDegree)
                    break;

                //try again
                time_interval*=2;
            }

        }

    }




}
