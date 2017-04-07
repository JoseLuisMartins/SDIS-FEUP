package network;

import file.Chunk;
import file.ChunkID;
import logic.*;
import management.FileManager;
import protocols.PutChunk;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
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

                handleReceivedMessage(msg,receivePacket.getAddress());


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


    public void handleReceivedMessage(Message msg,InetAddress senderAddress) {


        ChunkID chunkId= new ChunkID(msg.getFileId(), msg.getChunkNo());
        String version = msg.getVersion();

        switch (msg.getType()){
            case PUTCHUNK:
                if(!FileManager.isMyFile(msg.getFileId())){

                    System.out.println("[RECEIVED PUTCHUNK] Peer occupied space( " + getSizeOfBackupFolder() + ") fileId( " + msg.getFileId() + ") ,chunkNo(" + msg.getChunkNo() + ")");


                    //Enhancement 1 - Ensure the desired Replication Degree
                    Observer obs= new Observer(Utils.mc);



                    boolean hasChunk=isStoredChunk(chunkId);

                    if(((getSizeOfBackupFolder()+msg.getMessageBody().length) <= Utils.metadata.getMaximumDiskSpace()) && !hasChunk) {//check if storing the chunk will not overflow the backup space
                            Chunk chunk = new Chunk(msg.getFileId(),msg.getChunkNo(),msg.getMessageBody());
                            saveChunk(chunk);
                            Utils.metadata.addChunk(chunkId, msg.getReplicationDeg());
                            hasChunk=true;
                    }else if(!hasChunk)
                        System.out.println("-----------------Exceeded Allowed Backup Space-----------------" +
                                           "\nCan't store chunk(" + chunkId.getChunkID() + ") of the file (" + chunkId.getFileID() + ")");


                    if(hasChunk){
                        Utils.sleepRandomTime(400);

                        //Enhancement 1 - Ensure the desired Replication Degree
                        obs.stop();

                        int perceivedDegree=obs.getMessageNumber(MessageType.STORED, chunkId.getFileID(), chunkId.getChunkID());

                        if (perceivedDegree >= msg.getReplicationDeg()) {
                            Utils.metadata.removeChunk(chunkId);
                            FileManager.deleteChunk(chunkId);
                        }else {
                            Message response = new Message(MessageType.STORED, Utils.version, Utils.peerID, msg.getFileId(), msg.getChunkNo());
                            response.send(Utils.mc);
                        }
                    }
                }

                break;
            case GETCHUNK:

                if(isStoredChunk(chunkId)){

                    boolean withEnhancement=false;

                    if(!version.equals("1.0"))
                        withEnhancement=true;


                    Observer obs = new Observer(Utils.mdr);
                    Utils.sleepRandomTime(400);
                    obs.stop();

                    if(obs.getMessage(MessageType.CHUNK,msg.getFileId(),msg.getChunkNo()) == null) {//nobody has sent a chunk at the moment
                        Message response=null;
                        if (withEnhancement) {//Enhancement 2 - Chunks without body
                            try {
                                response = new Message(MessageType.CHUNK, version, Utils.peerID, msg.getFileId(), msg.getChunkNo());
                                Socket socket = new Socket(senderAddress,Utils.mdr.getPort());


                                OutputStream out = socket.getOutputStream();
                                DataOutputStream dos = new DataOutputStream(out);

                                byte[] chunk = loadChunk(chunkId);
                                dos.writeInt(chunk.length);
                                if (chunk.length > 0) {
                                    dos.write(chunk, 0, chunk.length);
                                }

                                System.out.println("Sending chunk over TCP fileId(" + msg.getFileId() +") , chunkNo(" +  msg.getChunkNo() + ")");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }else
                            response = new Message(MessageType.CHUNK,version,Utils.peerID,msg.getFileId(),msg.getChunkNo(),loadChunk(chunkId));


                        response.send(Utils.mdr);



                    }
                }

                break;
            case CHUNK:
                //it's all handled by the protocol
                break;
            case DELETE:
                String fileId = msg.getFileId();
                if(hasFileChunks(fileId)) {
                    deleteFileChunks(fileId);
                    Utils.metadata.removeFileChunks(fileId);

                    boolean withEnhancement=false;

                    if(!version.equals("1.0"))
                        withEnhancement=true;



                    if(withEnhancement) {//send confirmation message

                        try {
                            Socket socket = new Socket(senderAddress, Utils.mc.getPort());
                            OutputStream out = socket.getOutputStream();
                            DataOutputStream dos = new DataOutputStream(out);

                            Message m = new Message(MessageType.DELETED_CONFIRMATION, "2.0", Utils.peerID, fileId);
                            byte[] confirmation = m.getMessage();
                            dos.writeInt(confirmation.length);
                            if (confirmation.length > 0) {
                                dos.write(confirmation, 0, confirmation.length);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                break;
            case REMOVED:

                if(isStoredChunk(chunkId)) {
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

                    }
                }

                break;
            case STORED:
                if(FileManager.isStoredChunk(chunkId) || FileManager.isMyFile(chunkId.getFileID())) {//if it's either a file that i have backed up or a chunk that i have stored, update the rep degree
                    Utils.metadata.updateReplicationDegree(chunkId,msg.getSenderId(),true);//update metadata
                }

                break;
        }
    }



}
