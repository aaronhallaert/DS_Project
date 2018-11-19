package interfaces;

import Classes.Commando;
import Classes.Game;
import Classes.GameInfo;
import Classes.GameState;
import com.sun.org.apache.regexp.internal.RE;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface AppServerInterface extends Remote {

    int createGame(String activeUser, int dimensies, char set, int aantalSpelers) throws RemoteException;

    ArrayList<GameInfo> getGameInfoLijst() throws RemoteException;

    GameInfo getGameInfo(int gameId) throws RemoteException;

    GameState getGameState(int gameId) throws RemoteException;

    boolean join(String activeUser, int currentGameIdAttempt) throws RemoteException;

    boolean changeInTurn(int currentGameId, String userTurn) throws RemoteException;

    void executeFlipCommando(Commando commando, String activeUser, int currentGameId) throws RemoteException;

    public List<Commando> getInbox(String userName, int gameId) throws RemoteException;

    byte[] getImage(String naam)throws RemoteException;

    void storeImage(String naamFoto, byte[] afbeelding) throws RemoteException;

    boolean changeInPlayers(int currentGameId, int aantalSpelers) throws RemoteException;

    void leaveGame(int currentGameId, String username) throws RemoteException;

    Game getGame(int currentGameId) throws RemoteException;

    void spectate(int gameId, String username) throws RemoteException;

    void unsubscribeSpecator(int gameId, String username) throws RemoteException;
}
