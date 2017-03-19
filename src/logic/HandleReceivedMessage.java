package logic;


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

                    //TODO MARCELO --- guardar o chunk -> msg.getMessageBody(); talvez numa thread á partes uma vez que o acesso ao disco é puxado
                    //TODO if it's the first time add chunk replication degree to the chunk metadata file


                    Message response = new Message(MessageType.STORED,Utils.version,Utils.peerID,msg.getFileId(),msg.getChunkNo(),-1,null);
                    //Utils.sleepRandomTime(400);
                    response.send(Utils.mc);
                }

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
                if(peerIsTheSender){
                    //armazenar o chunk
                }

                break;
            case DELETE:
                //if(haschunk) remove it!



                break;
            case REMOVED:

                //if(hasChunk) update chunk metadata
                //if(delete < chunknumber) initiate putchunk

                break;
            case STORED:

                if(peerIsTheSender) {//it's the peer id
                    //update current putchunk info
                }else{
                    //TODO update replication degree of the chunk metadata file
                }

                break;
        }
    }
}
