package network;

import file.Chunk;
import file.ChunkID;
import logic.*;
import management.FileManager;
import protocols.Protocol;
import protocols.PutChunk;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

import static management.FileManager.*;


public class MulticastChannelWrapper implements Runnable{

    private MulticastSocket multicastSocket;
    private int port;
    private InetAddress address;
    private ChannelType type;
    private volatile boolean running; //thread safe variable
    private Vector<Observer> observers;


    public MulticastChannelWrapper(String address, String port,ChannelType type) throws IOException {
        this.type = type;
        this.observers = new Vector<>();
        this.running=true;

        //join multicast group
        this.port = Integer.parseInt(port);
        this.address = InetAddress.getByName(address);
        //join multicast group
        multicastSocket = new MulticastSocket(this.port);
        multicastSocket.joinGroup(this.address);

    }

    public void terminateLoop(){
        this.running=false;
    }

    public void closeSocket() throws IOException {
        multicastSocket.leaveGroup(address);
        multicastSocket.close();
    }

    public MulticastSocket getMulticastSocket() {
        return multicastSocket;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return address;
    }

    @Override
    public void run() {

        while(this.running){
            try {
                //receive message
                //todo check the length
                byte[] receiveData = new byte[64*1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                multicastSocket.receive(receivePacket);


                Message msg = new Message(Arrays.copyOf(receivePacket.getData(),receivePacket.getLength()));

                for (Observer obs: observers) {//notify observers's
                    obs.update(msg);
                }

                handleReceivedMessage(msg);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addObserver(Observer obs){
        observers.add(obs);
    }

    public void removeObserver(Observer obs){
        observers.remove(obs);
    }


    public void handleReceivedMessage(Message msg) {
        //    public Message(MessageType type, String version, int senderId, String fileId, int chunkNo, int replicationDeg,byte[] msgBody) {


        boolean peerIsTheSender=false; //it's the same peer who sent the request
        if(msg.getSenderId() == Utils.peerID)
            peerIsTheSender=true;

        ChunkID chunkId= new ChunkID(msg.getFileId(), msg.getChunkNo());

        switch (msg.getType()){
            case PUTCHUNK:
                if(!peerIsTheSender || hasChunk(chunkId)) {
                    // A peer must never store the chunks of its own files.
                    // but if it already has that chunk it means that the putchunk is sent not from the peer that has the original file,
                    // but from the peer that received a remove and it's trying to restore the chunk desired replication degree so it should send the STORED message

                    Chunk chunk = new Chunk(msg.getFileId(),msg.getChunkNo(),msg.getMessageBody());

                    System.out.println("PUTCHUNK [size of folder]-> " + getSizeOfBackupFolder());

                    boolean hasChunk=hasChunk(chunkId);
                    if(((getSizeOfBackupFolder()+msg.getMessageBody().length) <= Utils.metadata.getMaximumDiskSpace()) && !hasChunk) {//check if storing the chunk will not overflow the backup space
                            saveChunk(chunk);
                            Utils.metadata.addChunk(chunkId, msg.getReplicationDeg());
                            hasChunk=true;
                    }else if(!hasChunk)
                        System.out.println("-----------------Exceeded Allowed Backup Space-----------------" +
                                           "\nCan't store chunk(" + chunkId.getChunkID() + ") of the file (" + chunkId.getFileID() + ")");


                    if(hasChunk){
                        Message response = new Message(MessageType.STORED, Utils.version, Utils.peerID, msg.getFileId(), msg.getChunkNo());
                        Utils.sleepRandomTime(400);
                        response.send(Utils.mc);
                    }
                }

                break;
            case GETCHUNK:

                if(hasChunk(chunkId)){
                    Message response = new Message(MessageType.CHUNK,Utils.version,Utils.peerID,msg.getFileId(),msg.getChunkNo(),loadChunk(chunkId));
                    Observer obs = new Observer(Utils.mdr);
                    Utils.sleepRandomTime(400);
                    obs.stop();

                    if(obs.getMessage(MessageType.CHUNK,msg.getFileId(),msg.getChunkNo()) == null)//nobody has sent a chunk at the moment
                        response.send(Utils.mdr);
                }

                break;
            case CHUNK:
                //it's all handled the protocol
                break;
            case DELETE:
                String fileId = msg.getFileId();
                if(hasFileChunks(fileId))
                    deleteFileChunks(fileId);

                break;
            case REMOVED:

                if(hasChunk(chunkId)) {
                    Utils.metadata.updateReplicationDegree(chunkId,msg.getSenderId(),false);



                    if(Utils.metadata.getPerceivedDegree(chunkId) < Utils.metadata.getDesiredDegree(chunkId)) { //initiate putchunk
                        Observer obs = new Observer(Utils.mdb);
                        Utils.sleepRandomTime(400);
                        obs.stop();


                        if(obs.getMessage(MessageType.PUTCHUNK,msg.getFileId(),msg.getChunkNo()) == null){//nobody has initiated putchunk protocol
                            //Protocol.putChunkProtocol(new Chunk(chunkId.getFileID(),chunkId.getChunkID(),FileManager.loadChunk(chunkId)),Utils.metadata.getDesiredDegree(chunkId));
                            PutChunk pc = new PutChunk(new Chunk(chunkId.getFileID(),chunkId.getChunkID(),FileManager.loadChunk(chunkId)),Utils.metadata.getDesiredDegree(chunkId));
                            Thread threadPc = new Thread(pc);
                            threadPc.start();
                        }
                        System.out.println("exiting removed handler");
                    }
                }

                break;
            case STORED:
                if(hasChunk(chunkId)) {
                        Utils.metadata.updateReplicationDegree(chunkId,msg.getSenderId(),true);
                }

                break;
        }
    }



}
