package apps.cli;


import apps.common.CallBackInterface;
import apps.common.ServerInterface;


import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class BackupService extends UnicastRemoteObject implements ServerInterface {

    public static void main(String args[])  {
        System.out.println("Server");

        try {
            Registry reg = LocateRegistry.createRegistry(1099);
            BackupService service = new BackupService();
            reg.rebind("myServer", service);
            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private HashMap<String, CallBackInterface> clients = new HashMap<>();

    protected BackupService() throws RemoteException {
        super();
    }

    @Override
    public boolean join(String user, CallBackInterface client) throws RemoteException {
        if (clients.containsKey(user))
            return false;
        clients.put(user, client);


        client.notify(user + " is logged in");

        // tell other users that there is a new user
        notifyOthers(user, user + " joined");

        // tells this user who is logged in
        for (String name : clients.keySet())
            if (!name.equals(user))
                client.notify(name + " is logged in");
        return true;
    }

    @Override
    public void tell(String source, String message) throws RemoteException {
        notifyOthers(source, source + " said: " + message);
    }

    @Override
    public void leave(String name) throws RemoteException {
        clients.remove(name);
        notifyOthers(name, name + " leaved.");
    }

    private void notifyOthers(String source, String message) throws RemoteException {
        for (String name : clients.keySet())
            if (!name.equals(source))
                clients.get(name).notify(message);
    }



}
