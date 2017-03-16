package network;


import logic.ChannelType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class MulticastChannelWrapper implements Runnable{

    private MulticastSocket multicastSocket;
    private ChannelType type;


    public MulticastChannelWrapper(String address, String port,ChannelType type) throws IOException {
        this.type = type;

        //join multicast group
        int multicastPortNumber = Integer.parseInt(port);
        InetAddress multicastAddress = InetAddress.getByName(address);
        //join multicast group
        multicastSocket = new MulticastSocket(multicastPortNumber);
        multicastSocket.joinGroup(multicastAddress);

    }


    public void close() throws IOException {
        multicastSocket.leaveGroup(multicastSocket.getInetAddress());
        multicastSocket.close();
    }


    @Override
    public void run() {

        while(true){
            try {
                //receive message
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                multicastSocket.receive(receivePacket);

                //wait random time and start the thread to handle the message


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
