package protocols;


import file.Chunk;
import file.ChunkID;
import file.FileInfo;
import file.SplitFile;
import logic.ChunkState;
import logic.Message;
import logic.MessageType;
import logic.Utils;
import management.FileManager;

import java.io.File;
import java.io.IOException;
import java.util.*;


import static logic.Utils.sleepSpecificTime;
import static management.FileManager.restoreFile;

public class Protocol {
    public static int MAX_PUTCHUNK_TRIES = 5;
    public static int MAX_GETCHUNK_TRIES = 5;



    public static String startBackup(String pathName, int replicationDegree) throws IOException {
        SplitFile sf = new SplitFile(new File(pathName));
        ArrayList<Chunk> chunkList = sf.getChunksList();

        FileInfo info = new FileInfo(sf.getFileId(),replicationDegree, pathName, chunkList);
        Utils.metadata.addFile(info);

        for (int i = 0; i < chunkList.size(); i++){//for each chunk
            Chunk currentChunk = chunkList.get(i);

           // putChunkProtocol(currentChunk,replicationDegree);

            Utils.sleepSpecificTime(100);// because of io exception, to prevent the network overflow

            PutChunk pc = new PutChunk(currentChunk,replicationDegree);
            Thread threadPc = new Thread(pc);
            threadPc.start();

        }

        return "Backup handled sucessfully";
    }

/*
    public  static void putChunkProtocol(Chunk currentChunk,int replicationDegree) {
        int time_interval = 1000;
        ChunkID chunkId = currentChunk.getId();

        for (int j = 0; j < MAX_PUTCHUNK_TRIES; j++) {//maximum of 5 tries

            Message msg = new Message(MessageType.PUTCHUNK, Utils.version, Utils.peerID, chunkId.getFileID(), chunkId.getChunkID(), replicationDegree, currentChunk.getContent());

            network.Observer obs = new network.Observer(Utils.mc);
            msg.send(Utils.mdb);

            //wait 1 sec
            sleepSpecificTime(time_interval);
            //check responses
            obs.stop();
            System.out.println("Number-> " +  obs.getMessageNumber(MessageType.STORED,chunkId.getFileID(),chunkId.getChunkID()) + "\nchunk-> " + chunkId.toString() + "\nj-> " + j);

            if (obs.getMessageNumber(MessageType.STORED, chunkId.getFileID(), chunkId.getChunkID()) >= replicationDegree)
                break;

            //try again
            time_interval *= 2;
        }
    }*/

    public static String startRestore(String pathName){
        File f = new File(pathName);
        String fileId = Utils.sha256(f);

        int currChunk=0;
        ArrayList<Chunk> chunks=new ArrayList<>();



        while (true){
            Message msg = new Message(MessageType.GETCHUNK, Utils.version, Utils.peerID, fileId, currChunk);

            network.Observer obs = new network.Observer(Utils.mdr);

            for (int j = 0 ; j < MAX_GETCHUNK_TRIES; j++) {//maximum of 5 tries

                msg.send(Utils.mc);
                sleepSpecificTime(400);

                Message chunkMsg = obs.getMessage(MessageType.CHUNK,fileId,currChunk);
                if(chunkMsg != null) { // already received the chunk
                    chunks.add(new Chunk(fileId,currChunk,chunkMsg.getMessageBody()));
                    break;
                }

                if(j==MAX_GETCHUNK_TRIES-1)
                    return "Failed to get chunk number " + currChunk + "\n Exceeded number of tries";
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

    public static String startReclaim(int desiredSize){

        Utils.metadata.setMaximumDiskSpace(desiredSize);


        long occupiedSize = FileManager.getSizeOfBackupFolder();
        long spaceToFree = occupiedSize - desiredSize;

        System.out.println("Occupied size-> " + occupiedSize);

        if(spaceToFree > 0){

            ArrayList<ChunkState> sortedList = Utils.metadata.getSortedChunksToEliminate();


            for (int i = 0 ; i< sortedList.size(); i++){
                ChunkID currentChunk = sortedList.get(i).getChunkID();

                spaceToFree -= FileManager.getChunkSize(currentChunk);
                FileManager.deleteChunk(currentChunk);
                System.out.println("space to free-> " + spaceToFree);

                //remove chunk from metadata
                Utils.metadata.removeChunk(currentChunk);

                //send removed msg
                Message msg = new Message(MessageType.REMOVED, Utils.version, Utils.peerID, currentChunk.getFileID(),currentChunk.getChunkID());
                msg.send(Utils.mc);

                if (spaceToFree <= 0)//already freed enough space
                    break;

            }

        }

        /**/

        return "Reclaimed space handled sucessfully";
    }

    public static String startState(){

        StringBuilder state = new StringBuilder();

        state.append("-------- SERVICE STATE --------").append("\n");

        Iterator itFiles = Utils.metadata.getBackupFilesMetadata().entrySet().iterator();


            /*  For each File Should print:
                    -> pathname
                    -> id
                    -> replication degree
            */

        state.append("\n").append("-------- FILES SAVED ---------").append("\n");

        while(itFiles.hasNext()){
            Map.Entry pair = (Map.Entry)itFiles.next();

            FileInfo info = (FileInfo)pair.getValue();

            state.append("Path: ");
            state.append(info.getPath());

            state.append("   ID: ");
            state.append(pair.getKey());

            state.append("    Replication degree: ");
            state.append(info.getReplication());

            state.append("\n");

            state.append("Chunks: ").append("\n");
            for(int i = 0; i < info.getChunks().size(); i++){
                state.append("ID: ");
                state.append(info.getChunks().get(i).getId());

                state.append("  Replication: ");
                //TODO it is necessary to put the percieved replications degree

                state.append("\n");
            }
        }

        /*
            For each chunk should print:
                - id
                - size
                - replication degree
        */

        state.append("\n-------- CHUNKS SAVED-----\n");

        for(HashMap.Entry<String, HashSet<Integer>> pair :  Utils.metadata.getStoredChunksPerceivedDegree().entrySet()){

            state.append("     ID: ");
            state.append(pair.getKey());
/*
            System.out.print("Size: ");
            System.out.println(pair.getValue());
*/
            state.append("   Replication degree: ");
            state.append(pair.getValue().size());

            state.append("\n");
        }


        return state.toString();
    }

}
