package network;


import logic.*;
import org.omg.PortableInterceptor.INACTIVE;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;


public class MulticastChannelWrapper implements Runnable{

    private MulticastSocket multicastSocket;
    private int port;
    private InetAddress address;
    private ChannelType type;


    public MulticastChannelWrapper(String address, String port,ChannelType type) throws IOException {
        this.type = type;

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


                Thread request = new Thread(new HandleReceivedMessage(msg));
                request.start();



            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


}
