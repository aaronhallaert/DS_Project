package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DispatchInterface extends Remote {


    AppServerInterface loginUser(String naam, String paswoord) throws RemoteException;

    boolean userNameExists(String username) throws RemoteException;

    void insertUser(String username, String confirmPassword) throws RemoteException;
}
