package interfaces;

import Classes.Score;
import Classes.GameInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface DatabaseInterface extends Remote {

    /*---------- USER -------------------*/
    boolean userNameExists(String naam) throws RemoteException;
    void insertUser(String name, String password, boolean replicate) throws RemoteException;

    /*---------- CREDENTIALS ------------*/
    boolean checkUserCred(String naam, String paswoord) throws RemoteException;
    void createToken(String username, String password, boolean replicate) throws RemoteException;
    boolean isTokenValid(String username, String token) throws RemoteException;
    void cancelToken(String username, boolean replicate) throws RemoteException;
    String getToken(String username) throws RemoteException;


    /*---------- IMAGES -----------------*/
    byte[] getImage(String naam) throws RemoteException;
    void storeImage(String naamFoto, byte[] afbeelding, boolean replicate)throws RemoteException;

    /*---------- CONNECTIONS ------------*/
    void connectTo(DatabaseInterface toImpl) throws RemoteException;


    List<GameInfo> getGameInfoList(int currentSize) throws RemoteException;

    /*---------- GAMES ------------------*/
    void addGameInfo(GameInfo gameInfo, boolean replicate) throws RemoteException;
    void deleteGameInfo(int gameId, boolean b) throws RemoteException;
    void updateGameInfo(GameInfo gameInfo, boolean replicate ) throws RemoteException;
    List<GameInfo> getGameInfoList() throws RemoteException;

    /*---------- SCORES -----------------*/
    void updateScores(String username, int roosterSize, int eindScore, String command, boolean replicate) throws RemoteException;

    void insertScoreRow(String username, boolean replicate) throws RemoteException;

    boolean hasScoreRij(String username) throws RemoteException;

    ArrayList<Score> getScores() throws RemoteException;

    boolean ping() throws RemoteException;

}
