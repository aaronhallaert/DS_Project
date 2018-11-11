package interfaces;

import Classes.GameInfo;
import Classes.GameState;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface AppServerInterface extends Remote {

    void receiveHelloWorld(String test) throws RemoteException;

    int createGame(String activeUser, int dimensies, char set) throws RemoteException;

    ArrayList<GameInfo> getGameInfoLijst() throws RemoteException;

    GameInfo getGameInfo(int gameId) throws RemoteException;

    GameState getGameSate(int gameId) throws RemoteException;

    boolean join(String activeUser, int currentGameIdAttempt) throws RemoteException;

    boolean changeInPlayers(Integer aantalSpelersConnected, int currentGameId) throws RemoteException;


    //analoog aan https://github.com/aaronhallaert/DS_ChatRMI/blob/master/src/Server/ChatServiceImpl.java


    //ArrayList<Tile> getGameData(String activeUser);
}
