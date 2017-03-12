package apps.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallBackInterface extends Remote{

    public void notify(String message) throws RemoteException;
}
