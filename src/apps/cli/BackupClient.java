package apps.cli;


import apps.common.CallBackInterface;
import apps.common.ServerInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class BackupClient extends UnicastRemoteObject implements CallBackInterface {


    public BackupClient() throws RemoteException {
        super();
    }

    public void notify(String message) {
        System.out.println(message);

    }


    public static void main(String[] args) {
        try {
            // Obtains a reference for the remote object associated with the specified name.
            Registry reg = LocateRegistry.getRegistry("127.0.0.1",1099);
            ServerInterface proxy = (ServerInterface) reg.lookup("myServer");

            Scanner scanner = new Scanner(System.in);
            System.out.print("What's your nickname? ");
            String name = scanner.nextLine();
            if ( ! proxy.join(name, new BackupClient()) ) {
                System.out.println("Sorry, nickname is already in use.");
                return;
            }
            String message = scanner.nextLine();
            while(! message.equals("exit")) {
                if (!message.equals(""))
                    proxy.tell(name, message);
                message = scanner.nextLine();
            }
            proxy.leave(name);
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

}
