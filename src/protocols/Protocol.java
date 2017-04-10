package protocols;


import file.Chunk;
import file.ChunkID;
import file.FileInfo;
import file.SplitFile;
import logic.*;
import management.FileManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


import static logic.Utils.sleepSpecificTime;
import static management.FileManager.getChunkSize;
import static management.FileManager.getSizeOfBackupFolder;
import static management.FileManager.restoreFile;

public class Protocol {
    public static int MAX_PUTCHUNK_TRIES = 5;
    public static int MAX_GETCHUNK_TRIES = 5;



    public static String startBackup(String pathName, int replicationDegree,boolean withEnhancement) throws IOException {
        File f = new File(pathName);

        System.out.println("batatas");
        if(!f.exists())
            return  "The file you are trying to backup doesn't exist";

        SplitFile sf = new SplitFile(f);
        ArrayList<Chunk> chunkList = sf.getChunksList();


        FileInfo info = Utils.metadata.getFileInfo(sf.getFileId());

        if(info == null) {
            info = new FileInfo(sf.getFileId(),replicationDegree, pathName, chunkList.size());
            Utils.metadata.addFile(info);
        }


        for (int i = 0; i < chunkList.size(); i++){//for each chunk
            Chunk currentChunk = chunkList.get(i);


            Utils.sleepSpecificTime(1000);// because of io exception, to prevent the network overflow

            PutChunk pc = new PutChunk(currentChunk,replicationDegree,withEnhancement);
            Thread threadPc = new Thread(pc);
            threadPc.start();

        }

        return "Backup handled sucessfully";
    }

    public static void startBackUpForFailedFiles(){
        HashMap<String, FileInfo>  backedUpFilesMetadata= Utils.metadata.getBackupFilesMetadata();

        for(HashMap.Entry<String, FileInfo> entry : backedUpFilesMetadata.entrySet()) {
            String key = entry.getKey();
            FileInfo currFile = backedUpFilesMetadata.get(key);

            if(currFile.getIfBackupHasFailed()) {
                try {
                    startBackup(currFile.getPath(), currFile.getDesiredReplication(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public static String startRestore(String pathName,boolean withEnhancement){

        String res = null;
        String fileId = Utils.metadata.getFileIdByPathName(pathName);

        if(fileId != null) {
            int currChunk = 0;
            ArrayList<Chunk> chunks = new ArrayList<>();

            String version = Utils.version;

            if (withEnhancement)
                version = "2.0";

            ServerSocket welcomeSocket = null;
            try {
                welcomeSocket = new ServerSocket(Utils.mdr.getPort());
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true) {
                Message msg = new Message(MessageType.GETCHUNK, version, Utils.peerID, fileId, currChunk);

                network.Observer obs = new network.Observer(Utils.mdr);

                for (int j = 0; j < MAX_GETCHUNK_TRIES; j++) {//maximum of 5 tries

                    msg.send(Utils.mc);
                    sleepSpecificTime(400);
                    obs.stop();

                    Message chunkMsg = obs.getMessage(MessageType.CHUNK, fileId, currChunk);
                    if (chunkMsg != null) { // already received the chunk

                        if (withEnhancement) {
                            try { //get the chunk via tcp

                                Socket connectionSocket = welcomeSocket.accept();
                                //receive chunk
                                InputStream in = connectionSocket.getInputStream();
                                DataInputStream dis = new DataInputStream(in);

                                int len = dis.readInt();
                                byte[] chunkData = new byte[len];
                                if (len > 0) {
                                    dis.readFully(chunkData);
                                }

                                System.out.println("Received chunk via TCP with size(" + len + ")" + " ,fileId(" + fileId + ")" + " ,chunkNo(" + currChunk + ")");
                                chunks.add(new Chunk(fileId, currChunk, chunkData));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        } else {
                            System.out.println("Received chunk via Multicast with size(" + chunkMsg.getMessageBody().length + ")" + " ,fileId(" + fileId + ")" + " ,chunkNo(" + currChunk + ")");
                            chunks.add(new Chunk(fileId, currChunk, chunkMsg.getMessageBody()));
                        }
                        break;
                    }

                    if (j == MAX_GETCHUNK_TRIES - 1) {
                        try {
                            welcomeSocket.close();
                            return "Failed to get chunk number " + currChunk + "\n Exceeded number of tries";
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }


                if (chunks.get(currChunk).getContent().length < 64000)//it's the last chunk
                    break;

                currChunk++;
            }


            try {
                welcomeSocket.close();
                restoreFile(chunks, pathName);
            } catch (IOException e) {
                e.printStackTrace();
            }

            res="Restore handled sucessfully";
        }else
            res="Restore failed there is no such file with path:" + pathName;

        return res;
    }

    public static String startDelete(String pathName,boolean withEnhancement){
        String res=null;

        String fileId = Utils.metadata.getFileIdByPathName(pathName);


        FileInfo fileInfo = Utils.metadata.getFileInfo(fileId);

        if(fileInfo != null) {
            String version = "1.0";

            if(withEnhancement) {//wait for confirmation messages
                fileInfo.setDeleted(true);
                version="2.0";

                if( !Utils.confirmationDeleteThreadRunning) {
                    DeleteConfirmation deleteConfirmation = new DeleteConfirmation();
                    Thread confirmingThread = new Thread(deleteConfirmation);
                    confirmingThread.start();
                }
            }else//just delete the file
                Utils.metadata.removeFile(fileId);


            //todo thread to send delete until the file is fully deleted
            Message msg = new Message(MessageType.DELETE, version, Utils.peerID, fileId);
            msg.send(Utils.mc);

            res = "Deletion in progress";
        }else
            res = "The file you are trying to delete was not backed up";

        return res;
    }

    public static void deleteFilesNotFullyDeleted(){
        HashMap<String, FileInfo> backupFilesMetadata = Utils.metadata.getBackupFilesMetadata();

        for(HashMap.Entry<String, FileInfo> entry : backupFilesMetadata.entrySet()) {
            String key = entry.getKey();

            FileInfo currFileInfo = backupFilesMetadata.get(key);

            if(currFileInfo.isOnDeleteProcess()){
                startDelete(currFileInfo.getPath(),true);
            }
        }

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

        state.append("*******************Peer State*******************").append("\n");

        Iterator<Map.Entry<String,FileInfo>> itFiles = Utils.metadata.getBackupFilesMetadata().entrySet().iterator();


        state.append("\n").append("-------- Saved Files ---------").append("\n");

        while(itFiles.hasNext()){
            Map.Entry<String,FileInfo> pair = itFiles.next();

            FileInfo info = pair.getValue();

            state.append("Path: ");
            state.append(info.getPath());

            state.append("\nID: ");
            state.append(pair.getKey());

            state.append("\nDesired replication degree: ");
            state.append(info.getDesiredReplication());

            state.append("\n");

            state.append("Chunks: ---------").append("\n");
            for(int i = 0; i < info.getNChunks(); i++){
                state.append("\n\tID: " + i);


                state.append("   Perceived replication degree:: ");
                state.append(info.getPerceivedDegree(i));


                state.append("\n");
            }
        }



        state.append("\n-------- Chunks Saved-----\n");

        for(HashMap.Entry<String, HashSet<Integer>> pair :  Utils.metadata.getStoredChunksPerceivedDegree().entrySet()){
            ChunkID chunkID = new ChunkID(pair.getKey());
            state.append("ID: ");
            state.append(chunkID.getChunkID());


            state.append("\nSize(KBytes): ");
            state.append(getChunkSize(chunkID));


            state.append("\nPerceived replication degree: ");
            state.append(pair.getValue().size());

            state.append("\n");
        }

        state.append("\n-------- Space Used-----");
        state.append("\n\n Using " + getSizeOfBackupFolder()/1000 + " of " + Utils.metadata.getMaximumDiskSpace()/1000 + " KBytes.\n");


        state.append("************************************************").append("\n");
        return state.toString();
    }

}
