package network;


import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import file.Chunk;
import file.ChunkID;
import logic.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

import static logic.ChunkManager.manageChunkMessage;
import static management.FileManager.deleteFileChunks;
import static management.FileManager.hasFileChunks;
import static management.FileManager.saveChunk;
import static management.FileManager.hasChunk;


public class MulticastChannelWrapper implements Runnable{

    private MulticastSocket multicastSocket;
    private int port;
    private InetAddress address;
    private ChannelType type;
    private Vector<Observer> observers;


    public MulticastChannelWrapper(String address, String port,ChannelType type) throws IOException {
        this.type = type;
        this.observers = new Vector<>();

        //join multicast group
        this.port = Integer.parseInt(port);
        this.address = InetAddress.getByName(address);
        //join multicast group
        multicastSocket = new MulticastSocket(this.port);
        multicastSocket.joinGroup(this.address);

    }


    public void close() throws IOException {
        multicastSocket.leaveGroup(multicastSocket.getInetAddress());
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

        while(true){
            try {
                //receive message
                //todo check the length
                byte[] receiveData = new byte[64*1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                multicastSocket.receive(receivePacket);

                System.out.println("received-> " + type);
                Message msg = new Message(Arrays.copyOf(receivePacket.getData(),receivePacket.getLength()));
                handleReceivedMessage(msg);

                for (Observer obs: observers) {
                    obs.update(msg);
                }

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
                if(!peerIsTheSender) { // A peer must never store the chunks of its own files.

                    Chunk chunk = new Chunk(msg.getFileId(),msg.getChunkNo(),msg.getMessageBody());


                    if(!hasChunk(chunkId)) {
                        saveChunk(chunk); //verificar o espaço antes de guardar
                        Utils.metadata.addChunk(chunkId.toString(),msg.getReplicationDeg());
                    }

                    Message response = new Message(MessageType.STORED,Utils.version,Utils.peerID,msg.getFileId(),msg.getChunkNo());
                    Utils.sleepRandomTime(400);
                    response.send(Utils.mc);
                }
                //verificar no delete porque o putchunk pode ser um chunk meu

                break;
            case GETCHUNK:

                if(hasChunk(chunkId)){
                    Message response = new Message(MessageType.CHUNK,Utils.version,Utils.peerID,msg.getFileId(),msg.getChunkNo());
                    Utils.sleepRandomTime(400);
                    //verificar, se neste ponto já tiver sido recebida uma chunk message não enviar!
                    response.send(Utils.mc);
                }

                break;
            case CHUNK:
                //armazenar só se for meu
                //verificar se devo armazenar ao mandar os getchunks, guardar em algum lado
                //mandar para um chunk manager, que decide se guarda o chunk ou não
                manageChunkMessage(msg);

                break;
            case DELETE:
                String fileId = msg.getFileId();
                if(hasFileChunks(fileId))
                    deleteFileChunks(fileId);

                break;
            case REMOVED:

                //if(hasChunk) update chunk metadata
                //if(delete < chunknumber) initiate putchunk
                if(hasChunk(chunkId) && !peerIsTheSender) {//it's the peer id
                    Utils.metadata.updateReplicationDegree(chunkId.toString(),-1);
                    //if(replication degree < desired)
                }

                break;
            case STORED:

                //update metadata
                if(hasChunk(chunkId)) {//it's the peer id
                        Utils.metadata.updateReplicationDegree(chunkId.toString(),1);
                }

                break;
        }
    }




}
