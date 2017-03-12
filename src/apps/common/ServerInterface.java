package apps.common;


import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ServerInterface extends Remote{
     boolean join(String name, CallBackInterface c)throws RemoteException;
     void tell(String name, String message)throws RemoteException;
     void leave(String name)throws RemoteException;
}
