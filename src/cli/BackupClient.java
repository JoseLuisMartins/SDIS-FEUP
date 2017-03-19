package cli;


import common.CallBackInterface;
import common.Request;
import common.ServerInterface;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;
import java.util.TimerTask;


public class BackupClient extends UnicastRemoteObject implements CallBackInterface,Serializable {


    public BackupClient() throws RemoteException {
        super();
    }

    public void notify(String message) {
        System.out.println(message);

    }


    public static void main(String[] args) {
        //ex: java TestApp myServer BACKUP teste.txt 3
        //ex: java TestApp myServer RESTORE teste.txt

        if(args.length < 3){
            System.out.println('\n' + "-------- TestApp ------" + '\n');
            System.out.println("Usage: java TestApp <peer_name> <operation> <opnd_1> <opnd_2>");
            System.out.println("<peer_name> - peer access point (string where the server object was binded)");
            System.out.println("<operation> - BACKUP, RESTORE, DELETE, RECLAIM (ENH in the end in case of enhancemnt)");
            System.out.println("<opnd_1> - path name of the file or amount of space to reclaim.");
            System.out.println("<opnd_2> -  replication degree -> backup sub-protocol.");
            return;
        }

        //validate and contruct the request
        String accessPoint=args[0];


        Request request = new Request(args);


        try {


            // Obtains a reference for the remote object associated with the specified name.
            Registry reg = LocateRegistry.getRegistry("127.0.0.1",1099);
            ServerInterface initiatorPeer = (ServerInterface) reg.lookup(accessPoint);
            //send the request

            if(request.isValid())
                initiatorPeer.makeRequest(request, new BackupClient());
            else
                System.out.println("Invalid Request");

            System.exit(0);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }




}
