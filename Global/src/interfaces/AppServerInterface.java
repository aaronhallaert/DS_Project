package interfaces;

import Classes.Commando;
import Classes.GameInfo;
import Classes.GameState;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface AppServerInterface extends Remote {

    void receiveHelloWorld(String test) throws RemoteException;

    int createGame(String activeUser, int dimensies, char set) throws RemoteException;

    ArrayList<GameInfo> getGameInfoLijst() throws RemoteException;

    GameInfo getGameInfo(int gameId) throws RemoteException;

    GameState getGameSate(int gameId) throws RemoteException;

    boolean join(String activeUser, int currentGameIdAttempt) throws RemoteException;

    boolean changeInPlayers(Integer aantalSpelersConnected, int currentGameId) throws RemoteException;

    void executeFlipCommando(Commando commando, String activeUser, int currentGameId) throws RemoteException;

    public List<Commando> getInbox(String userName, int gameId) throws RemoteException;

    byte[] getImage(String naam)throws RemoteException;

    void storeImage(String naamFoto, byte[] afbeelding) throws RemoteException;

    boolean rejoin(String activeUser, int currentGameIdAttempt) throws RemoteException;

    //analoog aan https://github.com/aaronhallaert/DS_ChatRMI/blob/master/src/Server/ChatServiceImpl.java


    //ArrayList<Tile> getGameData(String activeUser);
}
