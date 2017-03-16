package cli;



import common.CallBackInterface;
import common.Request;
import common.ServerInterface;
import logic.ChannelType;
import logic.Utils;
import network.MulticastChannelWrapper;


import javax.rmi.CORBA.Util;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class BackupService extends UnicastRemoteObject implements ServerInterface {

    public static void main(String args[]) throws IOException {
        System.out.println("Initiating Peer");
        //ex: java TestApp 1.0 1 myServer  224.0.0.1 2222  224.0.0.2 2223 224.0.0.0 2224
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

        String version = args[0];
        int serverId= Integer.parseInt(args[1]);


        //subscribe multicast channels
        Utils.mc= new MulticastChannelWrapper(args[3],args[4], ChannelType.CONTROL_CHANNEL);
        Utils.mdb= new MulticastChannelWrapper(args[5],args[6],ChannelType.BACKUP_CHANNEL);
        Utils.mdr= new MulticastChannelWrapper(args[7],args[8],ChannelType.RESTORE_CHANNEL);

        /*
        //message debug
        //CR-13 LF-10
        Message msg = new Message("PUTCHUNK <Version> 5 <FileId> 6             <ReplicationDeg> " + Character.toString((char)13) + Character.toString((char)10));
        System.out.println(msg.toString());

        // public Message(MessageType type, String version, int senderId, String fileId, int chunkNo, String replicationDeg,String msgBody)
        //CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
        Message nMsg = new Message(MessageType.PUTCHUNK,"1.0",1,"hash_Sha256",1,"5","body");
        System.out.println(nMsg.toString());
        */


        BackupService service = new BackupService();

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
/*
        Thread threadMc = new Thread(Utils.mc);
        threadMc.start();

        Thread threadMdb = new Thread(Utils.mdb);
        threadMdb.start();

        Thread threadMdr = new Thread(Utils.mdr);
        threadMdr.start();*/


    }




    @Override
    public void makeRequest(Request req, CallBackInterface callBack) throws RemoteException {

        System.out.println(req.toString());
        //send messages to all the other peers based on the request


        callBack.notify("Request handled sucessfully");
    }





}
