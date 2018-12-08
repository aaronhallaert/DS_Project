package interfaces;


import Classes.Game;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface DispatchInterface extends Remote {

    /*---------- APPSERVER MANAGING ---------------------*/
    void registerAppserver(int portNumber) throws RemoteException;
    AppServerInterface giveAppserver() throws RemoteException;
    void newGameCreated() throws RemoteException;
    void gameFinished() throws RemoteException;


    /*----------- USER MANAGING --------------------------*/
    boolean userNameExists(String username) throws RemoteException;
    void insertUser(String username, String confirmPassword) throws RemoteException;


    /*----------- OVERZETTEN VAN ... ---------------------*/
    void changeGameServer(AppServerInterface appImpl, Game game) throws RemoteException;
    AppServerInterface changeClientServer(int currentGameIdAttempt) throws RemoteException;
    // verplaatsen naar appserver waar nog een game kan aangemaakt worden
    AppServerInterface changeClientServer(boolean dummy) throws RemoteException;
}
