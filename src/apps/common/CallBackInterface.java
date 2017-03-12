package apps.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallBackInterface extends Remote {

     void notify(String message) throws RemoteException;
}
