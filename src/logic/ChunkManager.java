package logic;


import file.Chunk;

import java.util.ArrayList;

import static management.FileManager.saveChunk;

public class ChunkManager {
    static ArrayList<String> validFileIds=new ArrayList<>();

    public static void manageChunkMessage(Message msg){
        if(validFileIds.contains(msg.getFileId())){
            Chunk chunk = new Chunk(msg.getFileId(),msg.getChunkNo(),msg.getMessageBody());
            saveChunk(chunk);
        }


    }

}
