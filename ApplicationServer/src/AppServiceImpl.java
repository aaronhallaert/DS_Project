import Classes.Game;
import Classes.GameInfo;
import Classes.GameState;
import interfaces.AppServerInterface;
import interfaces.DatabaseInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class AppServiceImpl extends UnicastRemoteObject implements AppServerInterface {

    private DatabaseInterface databaseImpl;

    private ArrayList<Game> gamesLijst; //game bevat GameInfo en GameState

    public AppServiceImpl() throws RemoteException{
        try {

            // setup communicatie met databaseserver
            // fire to localhost port 1900
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1901);
            // search for application service
            databaseImpl = (DatabaseInterface) myRegistry.lookup("DatabaseService");
        }
        catch(Exception e){
            e.printStackTrace();
        }

        gamesLijst = new ArrayList<Game>();
        System.out.println("gamesList created");

    }

    @Override
    public void receiveHelloWorld(String test) throws RemoteException {
        System.out.println(test);
    }

    @Override
    public int createGame(String activeUser, int dimensies, char set) throws RemoteException {

        System.out.println("createGame in appserviceImpl triggered");
        System.out.println(activeUser);

        //gameId maken, kijken als nog niet reeds bestaat
        int gameId = (int)(Math.random() * 1000);
        while(reedsGameMetDezeID(gamesLijst,gameId)){
            gameId = (int)(Math.random() * 1000);
        }




        Game game = new Game(gameId ,activeUser, dimensies, set);
        gamesLijst.add(game);
        System.out.println("game met naam "+activeUser+" gemaakt!");
        System.out.println("gameslist grootte is nu: "+gamesLijst.size());

        return gameId;

    }


    /**
     *
     * @param gamesLijst, de te checken lijst van games
     * @param gameId
     * @return  true als er al een game in deze lijst zit met deze gameId,
     *          false als er nog geen game in de lijst zit
     */
    private boolean reedsGameMetDezeID(ArrayList<Game> gamesLijst, int gameId) {

        for (Game game : gamesLijst) {
            if(game.getGameInfo().getGameId() == gameId){return true;}
        }
        return false;

    }

    /**

     * @return
     * @throws RemoteException
     */
    @Override
    public ArrayList<GameInfo> getGameInfoLijst() throws RemoteException {

        ArrayList<GameInfo> gameInfoLijst = new ArrayList<GameInfo>();

        for (Game game : gamesLijst) {
            gameInfoLijst.add(game.getGameInfo());
        }

        return gameInfoLijst;

    }

    @Override
    public GameInfo getGameInfo(int gameId) throws RemoteException {

        for (Game game : gamesLijst) {
            if(game.getGameId() == gameId){
                return game.getGameInfo();
            }
        }

        //als er geen game gevonden is met de gameId
        return new GameInfo(); // leeg object, herkennen dan in da spel daar
    }

    @Override
    public GameState getGameSate(int gameId) throws RemoteException {

        for (Game game : gamesLijst) {
            if(game.getGameId() == gameId){
                return game.getGameState();
            }
        }

        System.out.println("getGameState in AppserverImpl, mag niet gebeuren");
        return new GameState();
    }

    @Override
    public boolean join(String activeUser, int currentGameIdAttempt) throws RemoteException {
        return this.getGameInfo(currentGameIdAttempt).join(activeUser);


    }

    @Override
    public boolean changeInPlayers(Integer aantalSpelersConnected, int currentGameId) throws RemoteException {
        return this.getGameInfo(currentGameId).changeInPlayers(aantalSpelersConnected);
    }

    //analoog aan https://github.com/aaronhallaert/DS_ChatRMI/blob/master/src/Server/ChatServiceImpl.java




}
