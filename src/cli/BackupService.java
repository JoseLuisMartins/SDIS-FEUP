package cli;



import common.CallBackInterface;
import common.Request;
import common.ServerInterface;
import file.ChunkID;
import file.SplitFile;
import logic.*;
import network.MulticastChannelWrapper;


import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static management.FileManager.loadMetadata;
import static management.FileManager.saveMetadata;


public class BackupService extends UnicastRemoteObject implements ServerInterface {

    public static void main(String args[]) throws IOException {
        System.out.println("Initiating Peer");
        //ex: java TestApp 1.0 1 myServer  224.0.0.1 2222  224.0.0.2 2223 224.0.0.0 2224
        //java -jar McastSnooper.jar 224.0.0.1:2222  224.0.0.2:2223 224.0.0.0:2224
        if(args.length < 9){
            System.out.println('\n' + "-------- Peer ------" + '\n');
            System.out.println("Usage: java TestApp <protocol_version> <server_id> <service_acess_point> <MC_IP> <MC_Port> <MDB_IP> <MDB_Port> <MDR_IP> <MDR_Port>");
            System.out.println("<protocol_version> - ???");
            System.out.println("<server_id> - id");
            System.out.println("<service_acess_point> - string where the server object was binded");
            System.out.println("<MC_IP> - Multicast Control Channel IP");
            System.out.println("<MC_Port> - Multicast Control Channel Port");
            System.out.println("<MDB_IP> - Multicast Data Backup Channel IP");
            System.out.println("<MDB_Port> - Multicast Data Backup Channel Port");
            System.out.println("<MDR_IP> - Multicast Data Restore Channel IP");
            System.out.println("<MDR_Port> - Multicast Data Restore Channel Port");
            return;
        }


        //subscribe multicast channels and parse variables
        Utils.mc= new MulticastChannelWrapper(args[3],args[4], ChannelType.CONTROL_CHANNEL);
        Utils.mdb= new MulticastChannelWrapper(args[5],args[6],ChannelType.BACKUP_CHANNEL);
        Utils.mdr= new MulticastChannelWrapper(args[7],args[8],ChannelType.RESTORE_CHANNEL);
        Utils.version= args[0];
        Utils.peerID= Integer.parseInt(args[1]);
        Utils.peerSocket=new DatagramSocket();
        Utils.CHUNKS_FOLDER_NAME ="chunks_server_"+ Utils.peerID;
        loadMetadata();

        //shutdown thread
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                saveMetadata();
                System.out.println(Utils.metadata.toString());
            }
        }, "Shutdown-thread"));


        BackupService service = new BackupService();

        //bind remote object
        String accessPoint = args[2];

        if(!accessPoint.equals("default")){//when it's not a default peer (initiator peer)
            try {

                Registry reg = LocateRegistry.createRegistry(1099);
                reg.rebind(accessPoint, service);
                System.err.println("Peer ready");
            } catch (Exception e) {
                System.err.println("Peer exception: " + e.toString());
                e.printStackTrace();
            }
        }

    }




    protected BackupService() throws RemoteException {
        super();


        //start the threads

        Thread threadMc = new Thread(Utils.mc);
        threadMc.start();

        Thread threadMdb = new Thread(Utils.mdb);
        threadMdb.start();

        Thread threadMdr = new Thread(Utils.mdr);
        threadMdr.start();


    }




    @Override
    public void makeRequest(Request req, CallBackInterface callBack) throws RemoteException {
    /*
        System.out.println(req.toString());
        //send messages to all the other peers based on the request


        //message debug

        Message nMsg = new Message(MessageType.PUTCHUNK,Utils.version,Utils.senderID,"hash_Sha256",1,5,"body".getBytes(StandardCharsets.US_ASCII));
        System.out.println(nMsg.toString() + " \n \n \n");
        Message m= new Message(nMsg.getMessage());
        System.out.println("-----------------------------");
        System.out.println(m.toString() + " \n \n \n");


        try {
            nMsg.send(Utils.mc);
            nMsg.send(Utils.mdb);
        } catch (IOException e) {
            e.printStackTrace();
        }
    */
        switch (ProtocolType.valueOf(req.getOperation())){
            case BACKUP:
                File file = new File(req.getOpnd1());
                try {
                    SplitFile sp = new SplitFile(file);

                    for (int i = 0; i < sp.getChunksList().size(); i++){
                        ChunkID ck = sp.getChunksList().get(i).getId();
                        Message msg = new Message(MessageType.PUTCHUNK, Utils.version, Utils.peerID, ck.getFileID(),ck.getChunkID(),req.getReplication(),sp.getChunksList().get(i).getContent());
                        msg.send(Utils.mdb);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case DELETE:
                File f = new File(req.getOpnd1());
                String fileId = Utils.sha256(f.getName(),f.lastModified(),Utils.peerID);
                Message msg = new Message(MessageType.DELETE, Utils.version, Utils.peerID, fileId);
                msg.send(Utils.mc);
                break;
            case RECLAIM:

                break;
            case RESTORE:
                break;
        }




        callBack.notify("Request handled sucessfully");
    }






}
