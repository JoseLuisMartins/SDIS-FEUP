package logic;


import file.Chunk;
import file.ChunkID;

import static management.FileManager.deleteFileChunks;
import static management.FileManager.hasFileChunks;
import static management.FileManager.saveChunk;

public class HandleReceivedMessage implements Runnable {

    private Message msg;
    public HandleReceivedMessage(Message message) {
        this.msg = message;
    }

    @Override
    public void run() {
        //    public Message(MessageType type, String version, int senderId, String fileId, int chunkNo, int replicationDeg,byte[] msgBody) {


        boolean peerIsTheSender=false; //it's the same peer who sent the request
        if(msg.getSenderId() != Utils.peerID)
            peerIsTheSender=true;

        switch (msg.getType()){
            case PUTCHUNK:
                if(!peerIsTheSender) { // A peer must never store the chunks of its own files.

                    Chunk chunk = new Chunk(msg.getFileId(),msg.getChunkNo(),msg.getMessageBody());
                    saveChunk(chunk);


                    Message response = new Message(MessageType.STORED,Utils.version,Utils.peerID,msg.getFileId(),msg.getChunkNo(),-1,null);
                    Utils.sleepRandomTime(400);
                    response.send(Utils.mc);
                }
                //verificar no delete porque o putchunk pode ser um chunk meu

                break;
            case GETCHUNK:
                boolean hasChunk = false;

                if(hasChunk){

                    Message response = new Message(MessageType.CHUNK,Utils.version,Utils.peerID,msg.getFileId(),msg.getChunkNo(),-1,null);
                    Utils.sleepRandomTime(400);
                    //verificar, se neste ponto já tiver sido recebida uma chunk message não enviar!
                    response.send(Utils.mc);
                }

                break;
            case CHUNK:
                //armazenar só se for meu
                //verificar se devo armazenar ao mandar os getchunks, guardar em algum lado
                break;
            case DELETE:
                String fileId = msg.getFileId();
                if(hasFileChunks(fileId))
                    deleteFileChunks(fileId);

                break;
            case REMOVED:

                //if(hasChunk) update chunk metadata
                //if(delete < chunknumber) initiate putchunk

                break;
            case STORED:
                ChunkID chunkId= new ChunkID(msg.getFileId(), msg.getChunkNo());

                if(peerIsTheSender) {//it's the peer id
                    //update current putchunk info
                }else{
                    Utils.metadata.incReplicationDegree(chunkId);
                }

                break;
        }
    }
}
