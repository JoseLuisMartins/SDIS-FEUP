package network;


import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class MulticastChannelWrapper {

    private MulticastSocket multicastSocket;


    public MulticastChannelWrapper(String address, String port) throws IOException {
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

    public void startWaitThread(){

    }


}
