package network;


import file.Chunk;
import file.ChunkID;
import file.SplitFile;
import logic.Message;
import logic.MessageType;
import logic.Utils;
import management.FileManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


import static logic.Utils.sleepSpecificTime;
import static management.FileManager.restoreFile;

public class Protocol {
    public static int MAX_PUTCHUNK_TRIES = 5;
    public static int MAX_GETCHUNK_TRIES = 5;



    public static String startBackup(String pathName, int replicationDegree) throws IOException {
        SplitFile sf = new SplitFile(new File(pathName));
        ArrayList<Chunk> chunkList = sf.getChunksList();


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
                //System.out.println("Number-> " +  obs.getMessageNumber(MessageType.STORED,chunkId.getFileID(),chunkId.getChunkID()) + "\nchunk-> " + chunkId.toString() + "\nj-> " + j);

                if(obs.getMessageNumber(MessageType.STORED,chunkId.getFileID(),chunkId.getChunkID()) >= replicationDegree)
                    break;

                //try again
                time_interval*=2;
            }

        }

        return "Backup handled sucessfully";

    }

    public static String startRestore(String pathName){
        File f = new File(pathName);
        String fileId = Utils.sha256(f);

        int currChunk=0;
        ArrayList<Chunk> chunks=new ArrayList<>();



        while (true){
            Message msg = new Message(MessageType.GETCHUNK, Utils.version, Utils.peerID, fileId, currChunk);

            Observer obs = new Observer(Utils.mdr);

            for (int j = 0 ; j < MAX_GETCHUNK_TRIES; j++) {//maximum of 5 tries

                msg.send(Utils.mc);
                sleepSpecificTime(400);

                Message chunkMsg = obs.getMessage(MessageType.CHUNK,fileId,currChunk);
                if(chunkMsg != null) { // already received the chunk
                    chunks.add(new Chunk(fileId,currChunk,chunkMsg.getMessageBody()));
                    break;
                }

                if(j==MAX_GETCHUNK_TRIES-1)
                    return "Failed to get chunk number " + j + "\n Exceeded number of tries";
            }



            obs.stop();


            if(chunks.get(currChunk).getContent().length < 64000)//it's the last chunk
                break;

            currChunk++;
        }



        try {
            restoreFile(chunks,"[RESTORED]" + pathName );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Restore handled sucessfully";
    }

    public static String startDelete(String pathname){
        File f = new File(pathname);
        String fileId = Utils.sha256(f);
        Message msg = new Message(MessageType.DELETE, Utils.version, Utils.peerID, fileId);
        msg.send(Utils.mc);

        return "Delete handled sucessfully";
    }

    public static String startReclaim(int size){

        File fol = new File("out");
        System.out.println("Arg-> " + size*1000);
        System.out.println("size-> " + FileManager.getSizeOfFolder(fol));

        //determine space to free
        //get metadata sorted by replication degree
        //delete the file
        //send removed
        //until space to free is reached







        return "Reclaimed space handled sucessfully";
    }

    public static String startState(){



        return "State handled";
    }






}
