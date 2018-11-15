package interfaces;


import java.rmi.Remote;
import java.rmi.RemoteException;


public interface DispatchInterface extends Remote {


    void logoutUser(String username) throws RemoteException;

    AppServerInterface loginUser(String naam, String paswoord) throws RemoteException;

    boolean userNameExists(String username) throws RemoteException;

    void insertUser(String username, String confirmPassword) throws RemoteException;



    String getToken(String username) throws RemoteException;

    AppServerInterface loginWithToken(String token, String username) throws RemoteException;
}
