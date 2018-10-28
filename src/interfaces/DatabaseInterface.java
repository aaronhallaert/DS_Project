package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DatabaseInterface extends Remote {

    boolean checkUserCred(String naam, String paswoord) throws RemoteException;
    boolean userNameExists(String naam) throws RemoteException;
    void insertUser(String name, String password) throws RemoteException;

    String createToken(String username, String password) throws RemoteException;

    boolean isTokenValid(String username, String token) throws RemoteException;
}
