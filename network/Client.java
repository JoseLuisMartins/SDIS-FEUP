/**
 * Created by josemartins on 17-02-2017.
 */


import java.net.*;
import java.util.ArrayList;

import static java.lang.Integer.parseInt;


/*
Client


The client must be invoked as follows:

java Client <host_name> <port_number> <oper> <opnd>*
where
<host_name> is the name of the host running the server;
<port_number> is the server port;
<oper> is either ‘‘register’’or ‘‘lookup’’
<opnd>* is the list of arguments
<plate number> <owner name>, for register;
<plate number>, for lookup.
After submitting a request, the client waits to receive a reply to the request, prints the reply, and then terminates.

The client application should also print messages in the screen to check if it is working as expected, with the following the format:

<oper> <opnd>*: <result>
where <oper> and <opnd*> are the arguments of the request
<result>
is the value returned by the server or ‘‘ERROR’’ if an error occurs.
*/

public class Client {


    public static void main(String args[]) throws Exception {
        /*
        if(args.length > 5 || args.length < 3){
            System.out.println( "Wrong number of arguments \n" +
                    "USAGE: java Client <host_name> <port_number> <per> <opnd>* \n" +
                    "<opnd>* is the list of arguments\n" +
                    "<plate number> <owner name>, for register;\n" +
                    "<plate number>, for lookup.");
            return;
        }*/

        //java client <mcast_addr> <mcast_port> <oper> <opnd> *


        String mcast_addr = args[0];
        int mcast_port = parseInt(args[1]);
        String operation = args[2];

        if(operation.equals("register") && args.length != 5){
            System.out.println("To register your vehicle in the server you must provide your plate-number and name");
            return;
        }


        ArrayList<String> options = new ArrayList<String>();

        //plate number
        options.add(args[3]);

        if(args.length == 5)
            options.add(args[4]);


        Client client = new Client(mcast_addr,mcast_port);
        client.makeRequest(operation,options);


    }


    String mcast_addr;
    int mcast_port;
    Socket clientSocket;


    public Client(String hostname, int mcast_port) {

        this.mcast_addr = hostname;
        this.mcast_port = mcast_port;
        this.clientSocket = new Socket();

    }

    public void makeRequest(String requestType,ArrayList<String> options) throws Exception {
        //join multicast group
        MulticastSocket multicastSocket = new MulticastSocket(this.mcast_port);
        InetAddress mCastIpAddress = InetAddress.getByName(this.mcast_addr);
        multicastSocket.joinGroup(mCastIpAddress);

        //get server advertise
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        multicastSocket.receive(receivePacket);
        multicastSocket.leaveGroup(mCastIpAddress);
        multicastSocket.close();

        String serverAdvertise = new String(receivePacket.getData());
        System.out.println("Server advertise: " + serverAdvertise);


        //construct request message

        String resquestString= "";

        if(requestType.equals("register")) {
            resquestString = "REGISTER " + options.get(0) + " " + options.get(1);
        }else if(requestType.equals("lookup")){
            resquestString = "LOOKUP " + options.get(0) + " wtf";
        }else
            return;

        DatagramSocket clientSocket = new DatagramSocket();

        // send request
        byte[] sendData = resquestString.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
        clientSocket.send(sendPacket);


        //get server response

        byte[] requestResponseData = new byte[1024];

        DatagramPacket requestResponsePacket = new DatagramPacket(requestResponseData, requestResponseData.length);
        clientSocket.receive(requestResponsePacket);

        String requestResponse = new String(requestResponsePacket.getData());
        System.out.println("Server request response: " + requestResponse);



        clientSocket.close();


    }

}
