package interfaces;

import Classes.Score;
import Classes.GameInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface DatabaseInterface extends Remote {

    /*---------- USER -------------------*/
    boolean userNameExists(String naam) throws RemoteException;
    void insertUser(String name, String password) throws RemoteException;

    /*---------- CREDENTIALS ------------*/
    boolean checkUserCred(String naam, String paswoord) throws RemoteException;
    void createToken(String username, String password) throws RemoteException;
    boolean isTokenValid(String username, String token) throws RemoteException;
    void cancelToken(String username) throws RemoteException;
    String getToken(String username) throws RemoteException;


    /*---------- IMAGES -----------------*/
    byte[] getImage(String naam) throws RemoteException;
    void storeImage(String naamFoto, byte[] afbeelding)throws RemoteException;

    /*---------- CONNECTIONS ------------*/
    void connectTo(DatabaseInterface toImpl) throws RemoteException;


    /*---------- GAMES ------------------*/
    void addGameInfo(GameInfo gameInfo) throws RemoteException;
    void updateGameInfo(GameInfo gameInfo) throws RemoteException;


    /*---------- SCORES -----------------*/
    void updateScores(String username, int roosterSize, int eindScore, String command) throws RemoteException;

    void insertScoreRow(String username) throws RemoteException;

    boolean hasScoreRij(String username) throws RemoteException;

    ArrayList<Score> getScores() throws RemoteException;

    boolean ping() throws RemoteException;
}
