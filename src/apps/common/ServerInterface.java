package apps.common;


import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ServerInterface extends Remote{
    public boolean join(String name, CallBackInterface c)throws RemoteException;
    public void tell(String name, String message)throws RemoteException;
    public void leave(String name)throws RemoteException;
}
