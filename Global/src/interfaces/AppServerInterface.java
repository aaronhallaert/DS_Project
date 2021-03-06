package interfaces;

import Classes.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface AppServerInterface extends Remote {

    /*----------- APPSERVER INFO ---------------------*/
    int getPortNumber() throws RemoteException;
    boolean testConnection() throws RemoteException;
    void close() throws RemoteException;


    /*----------- USER MANAGER -----------------------*/
    boolean loginUser(String naam, String paswoord) throws RemoteException;
    boolean loginWithToken(String token, String username) throws RemoteException;
    void logoutUser(String username) throws RemoteException;
    String getToken(String username) throws RemoteException;


    /*----------- GAME INFO -----------------------*/
    int createGame(String activeUser, int dimensies, char set, int aantalSpelers) throws RemoteException;



    int getNumberOfGames() throws RemoteException;
    ArrayList<GameInfo> getGameInfoLijst(boolean dummy) throws RemoteException; // enkel bij verandering
    ArrayList<GameInfo> getGameInfoLijst() throws RemoteException;  // instant
    ArrayList<Game> getGamesLijst() throws RemoteException;

    Game getGame(int currentGameId) throws RemoteException;
    GameInfo getGameInfo(int gameId) throws RemoteException;
    GameState getGameState(int gameId) throws RemoteException;

    boolean hasGame(int gameId) throws RemoteException;


    /*----------- GAME ACTIONS --------------------*/
    boolean join(String activeUser, int currentGameIdAttempt) throws RemoteException;
    void leaveGame(int currentGameId, String username) throws RemoteException;

    boolean changeInPlayers(int currentGameId, int aantalSpelers) throws RemoteException;
    boolean changeInTurn(int currentGameId, String userTurn) throws RemoteException;
    void executeFlipCommando(Commando commando, String activeUser, int currentGameId) throws RemoteException;
    List<Commando> getInbox(String userName, int gameId) throws RemoteException;

    void spectate(int gameId, String username) throws RemoteException;
    void unsubscribeSpecator(int gameId, String username) throws RemoteException;

    /*---------- ASK/PUSH METADATA --------------------*/
    byte[] getImage(String naam)throws RemoteException;
    void storeImage(String naamFoto, byte[] afbeelding) throws RemoteException;



    /*---------- HANDLING GAMES INTERNALLY ------------*/
    void takeOverGame(Game game) throws RemoteException;
    void deleteGame(int gameId) throws RemoteException;
    void deleteBackupGame(int gameId) throws RemoteException;
    void removeGameFromRunningGames(Game game) throws RemoteException;

    /*---------- HANDLINE SCORE TABLES ----------------*/
    ArrayList<Score> getScores() throws RemoteException;

    /*---------- BACKUP -------------------------------*/
    void takeBackupFrom(int appserverpoort) throws RemoteException;

    void setDestinationBackup(int appBackupPoort) throws RemoteException;

    void updateBackupGS(GameState gameState) throws RemoteException;

    BackupGames getBackup() throws RemoteException;

    void notifyGameInfoList() throws RemoteException;

    boolean prepareForNewGame() throws RemoteException;

    void addGameInBackup(Game game) throws RemoteException;

    AppServerInterface getDestinationBackup() throws RemoteException;
}
