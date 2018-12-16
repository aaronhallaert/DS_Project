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
    void insertUser(String name, String password, boolean onMaster) throws RemoteException;

    /*---------- CREDENTIALS ------------*/
    boolean checkUserCred(String naam, String paswoord) throws RemoteException;
    void createToken(String username, String password, boolean onMaster) throws RemoteException;
    boolean isTokenValid(String username, String token) throws RemoteException;
    void cancelToken(String username, boolean onMaster) throws RemoteException;
    String getToken(String username) throws RemoteException;


    /*---------- IMAGES -----------------*/
    byte[] getImage(String naam) throws RemoteException;
    void storeImage(String naamFoto, byte[] afbeelding, boolean onMaster)throws RemoteException;

    /*---------- CONNECTIONS ------------*/
    void connectTo(DatabaseInterface toImpl) throws RemoteException;

    List<GameInfo> getGameInfoList(boolean dummy) throws RemoteException;

    /*---------- GAMES ------------------*/
    void addGameInfo(GameInfo gameInfo, boolean onMaster) throws RemoteException;
    void deleteGameInfo(int gameId, boolean onMaster) throws RemoteException;
    void updateGameInfo(GameInfo gameInfo, boolean onMaster ) throws RemoteException;
    List<GameInfo> getGameInfoList() throws RemoteException;

    /*---------- SCORES -----------------*/
    void updateScores(String username, int roosterSize, int eindScore, String command, boolean onMaster) throws RemoteException;

    void insertScoreRow(String username, boolean onMaster) throws RemoteException;

    boolean hasScoreRij(String username) throws RemoteException;

    ArrayList<Score> getScores() throws RemoteException;

    boolean ping() throws RemoteException;

}
