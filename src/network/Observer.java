package network;



import logic.Message;
import logic.MessageType;

import java.util.ArrayList;
import java.util.Vector;


public class Observer {

    private Vector<Message> receivedMessages;
    private MulticastChannelWrapper channelToObserve;

    public Observer(MulticastChannelWrapper channelToObserve) {
        this.channelToObserve = channelToObserve;
        receivedMessages=new Vector<>();
        channelToObserve.addObserver(this);
    }

    public void update(Message msg){
        receivedMessages.add(msg);
    }

    public Message getMessage(MessageType type , String FileId, int chunkNo){//for chunk
        Message res=null;

        for (Message m: receivedMessages) {
            if(m.getType() == type && FileId.equals(m.getFileId()) && chunkNo == m.getChunkNo()) {
                res=m;
                break;
            }
        }

        return res;
    }

    public int getMessageNumber(MessageType type , String FileId, int chunkNo){//for stored
        int res=0;
        for (Message m: receivedMessages) {
            if(m.getType() == type && FileId.equals(m.getFileId()) && chunkNo == m.getChunkNo())
                res++;
        }

        return res;
    }



    public boolean existsType(MessageType type) {
        for (Message m: receivedMessages) {
            if(m.getType() == type)
                return true;
        }

        return false;
    }


    public void stop(){
        channelToObserve.removeObserver(this);
    }


}
