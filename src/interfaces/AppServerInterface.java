package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AppServerInterface extends Remote {

    void receiveHelloWorld(String test) throws RemoteException;


}
