package logic;


import file.Chunk;


import java.util.Vector;

import static management.FileManager.saveChunk;

public class ChunkManager {
    public static Vector<String> validFileIds=new Vector<>();

    public static boolean manageChunkMessage(Message msg){
        if(validFileIds.contains(msg.getFileId())){
            System.out.println("\n\n---------manageChunkMessage--------\n chunkno-> " + msg.getChunkNo() + "\n\n");
            Chunk chunk = new Chunk(msg.getFileId(),msg.getChunkNo(),msg.getMessageBody());
            saveChunk(chunk);
            return true;
        }else
            return false;
    }

}
