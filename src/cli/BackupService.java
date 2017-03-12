package cli;



import common.CallBackInterface;
import common.Request;
import common.ServerInterface;


import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class BackupService extends UnicastRemoteObject implements ServerInterface {

    public static void main(String args[]) throws RemoteException {
        System.out.println("Initiating Peer");

        if(false && args.length < 9){
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


        //join multicast group
       /* MulticastSocket multicastSocket = new MulticastSocket(this.mcast_port);
        InetAddress mCastIpAddress = InetAddress.getByName(this.mcast_addr);
        multicastSocket.joinGroup(mCastIpAddress);*/



        BackupService service = new BackupService();
        service.subscribeMulticastChannels();


        //when it's the initiator peer
        try {
            Registry reg = LocateRegistry.createRegistry(1099);
            reg.rebind("myServer", service);
            System.err.println("Peer ready");
        } catch (Exception e) {
            System.err.println("Peer exception: " + e.toString());
            e.printStackTrace();
        }
    }


    protected BackupService() throws RemoteException {
        super();
    }



    public void subscribeMulticastChannels(){

    }

    @Override
    public void makeRequest(Request req, CallBackInterface callBack) throws RemoteException {
        //notify all the other peers based on the request

        System.out.println(req.toString());
        callBack.notify("Request handled sucessfully");
    }





}
