package cli;



import common.CallBackInterface;
import common.Request;
import common.ServerInterface;
import logic.*;
import management.FileManager;
import network.MulticastChannelWrapper;
import protocols.Protocol;


import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


@SuppressWarnings("serial")
public class BackupService extends UnicastRemoteObject implements ServerInterface {


    public static void main(String args[]) throws IOException {
        System.out.println("Initiating Peer");
        // java TestApp 1.0 1 myServer  224.0.0.1 2222  224.0.0.2 2223 224.0.0.0 2224
        // java -jar McastSnooper.jar 224.0.0.1:2222  224.0.0.2:2223 224.0.0.0:2224


        if(!Utils.validServiceArgs(args)){
            System.out.println('\n' + "-------- Peer ------" + '\n');
            System.out.println("Usage: java TestApp <protocol_version> <server_id> <service_acess_point> <MC_IP> <MC_Port> <MDB_IP> <MDB_Port> <MDR_IP> <MDR_Port>");
            System.out.println("<protocol_version>");
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
        Utils.confirmationDeleteThreadRunning = false;
        FileManager.loadMetadata();

        System.out.println('\n' + "-------- Peer" +  Utils.peerID + " ready ------" + '\n');



        BackupService service = new BackupService();
        //bind remote object
        String accessPoint = args[2];

        //check if there are still files to delete
        // ------ DELETE ENHANCEMENT -------
        Protocol.deleteFilesNotFullyDeleted();
        //------------------------
        //------- RECLAIM ENHANCEMENT ----------
        Protocol.startBackUpForFailedFiles();


        try {

            Registry reg = LocateRegistry.getRegistry();
            reg.rebind(accessPoint, service);
            System.err.println("Object binded with accessPoint-> " + accessPoint);

        } catch (Exception e) {
            try {
                Registry reg = LocateRegistry.createRegistry(1099);
                reg.rebind(accessPoint, service);
                System.err.println("Object binded with accessPoint: " + accessPoint);
            } catch (Exception e2) {
                System.err.println("Peer exception: \n" + e.toString());
                e.printStackTrace();
            }
        }


        //shutdown thread
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                if(service != null) {
                    /*try {//CLose all the sockets
                       service.terminateMulticastThreads();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }/**/
                }

                FileManager.saveMetadata();
                System.out.println(Utils.metadata.toString());

            }
        }, "Shutdown-thread"));

    }


    private Thread threadMc;
    private Thread threadMdb;
    private Thread threadMdr;

    protected BackupService() throws RemoteException {
        super();


        //start the threads

        threadMc = new Thread(Utils.mc);
        threadMc.start();

        threadMdb = new Thread(Utils.mdb);
        threadMdb.start();

        threadMdr = new Thread(Utils.mdr);
        threadMdr.start();

    }

    public void terminateMulticastThreads() throws InterruptedException, IOException {
        Utils.mdb.terminateLoop();
        Utils.mdr.terminateLoop();
        Utils.mc.terminateLoop();

        threadMdb.join();
        threadMdr.join();
        threadMc.join();


        Utils.mdb.closeSocket();
        Utils.mdr.closeSocket();
        Utils.mc.closeSocket();
    }



    @Override
    public void makeRequest(Request req, CallBackInterface callBack) throws RemoteException {

        String answer="";
        switch (req.getOperation()){
            case BACKUP:
                try {
                    File f = new File(req.getOpnd1());

                    if(Utils.metadata.getBackupFilesMetadata().get(Utils.sha256(f)) != null)
                        answer = "This file already was backed up";
                    else
                        answer=Protocol.startBackup(req.getOpnd1(), req.getReplication(),req.isEnhancement());
                } catch (IOException e) {
                    System.out.println("A problem ocurred with the file you are trying to backup\n\nDescription of the Exception: \n");
                    e.printStackTrace();
                }
                break;
            case RESTORE:
                answer=Protocol.startRestore(req.getOpnd1(),req.isEnhancement());
                break;
            case DELETE:
                answer = Protocol.startDelete(req.getOpnd1(),req.isEnhancement());
                break;
            case RECLAIM:
                answer=Protocol.startReclaim(Integer.parseInt(req.getOpnd1())*1000);
                break;
            case STATE:
                answer = Protocol.startState();
                break;
        }



        callBack.notify(answer);
    }

}
