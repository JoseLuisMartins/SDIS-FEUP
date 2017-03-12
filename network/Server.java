
import java.net.*;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


import static java.lang.Integer.parseInt;

public class Server {


    public static void main(String args[]) throws Exception {
        int portNumber = parseInt(args[0]);
        String multicastAddress=args[1];
        int multicastPort= parseInt(args[2]);


        Server server = new Server(portNumber,multicastAddress,multicastPort);
        server.listen();

    }

    //server

    int portNumber;
    String multicastAddress;
    int multicastPort;
    HashMap<String,String> database;
    DatagramSocket serverSocket;

    public Server(int portNumber,String multicastAddress , int multicastPort) throws SocketException {
        this.portNumber = portNumber;
        this.multicastAddress=multicastAddress;
        this.multicastPort = multicastPort;
        this.database = new HashMap<>();
        this.serverSocket = new DatagramSocket();
    }

    public void advertise() {

        try {
            System.out.println("Advertising");
            InetAddress address = InetAddress.getByName(this.multicastAddress);

            byte[] answer = "Boas sou o server!!!".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(answer, answer.length, address, this.multicastPort);
            this.serverSocket.send(sendPacket);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void listen() throws Exception{



        while(true){

            //advertise on each 2 seconds
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    //advertise the server location to the multicast group
                    advertise();
                }
            }, 0, 4000);


            byte[] receiveData = new byte[1024];

            //obter request
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            this.serverSocket.receive(receivePacket);
            String request = new String(receivePacket.getData());
            System.out.println("RECEIVED: " + request);


            //resposta ao request
            String answer = handleRequest(request);
            byte[] sendData = answer.getBytes();
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            this.serverSocket.send(sendPacket);
        }
    }


    public String handleRequest(String request){
        String response="";

        String[] parts = request.split(" ");
        for (int i =0;i<parts.length; i++)
            parts[i].replaceAll("\\s+","");


        if(parts[0].equals("REGISTER")){
            this.database.put(parts[1],parts[2]);
            response = Integer.toString(this.database.size());
        }else {
            if(this.database.containsKey(parts[1]))
                response += "Plate number: " + parts[1] + " Owner: " + this.database.get(parts[1]);
            else
                response +="ERROR";

        }

        return response;
    }


}
