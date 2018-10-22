package appserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AppServerInterface extends Remote {

    void receiveHelloWorld(String test) throws RemoteException;

    boolean loginUser(String naam, String paswoord) throws RemoteException;

    boolean userNameExists(String username) throws RemoteException;

    void insertUser(String username, String confirmPassword) throws RemoteException;
}
