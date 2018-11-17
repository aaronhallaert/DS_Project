import Classes.Commando;
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
import java.util.List;

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


            // load alle games
            gamesLijst=databaseImpl.getGames();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        //gamesLijst = new ArrayList<>();
        System.out.println("gamesList created");

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

        // TODO update database met deze game

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
    public Game getGame(int currentGameId) {
        for (Game game : gamesLijst) {
            if(game.getGameId()==currentGameId){
                return game;
            }
        }

        return null;
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

        //aanpassing omdat er ook nog moet gejoined worden in de gameState
        if(this.getGameInfo(currentGameIdAttempt).join(activeUser)){
            // als de if clause lukt,
            // dan zal het getGameState ook lukken, daarom is getGameState een void
            this.getGameSate(currentGameIdAttempt).join(activeUser);
            //setGameStatenaam nog
            return true;
        }
        else {
            System.out.println("joinen loopt fout in appServiceImpl.java");
            return false;
        }

    }

    @Override
    public boolean changeInPlayers(int currentGameId, int aantalSpelers) throws RemoteException{
        if(this.getGameInfo(currentGameId).changeInPlayers(aantalSpelers)){
            System.out.println("er is verandering in users ontdekt");
            return true;
        }else{
            System.out.println("er geen verandering in users ontdekt");
            return false;
        }

    }

    @Override
    public void leaveGame(int currentGameId, String username) throws RemoteException {
        this.getGameInfo(currentGameId).playerLeaves();
        databaseImpl.pushGames(gamesLijst);
    }

    @Override
    public boolean changeInTurn(int currentGameId, char userTurn) throws RemoteException{
        return this.getGameSate(currentGameId).changeInTurn(userTurn);
    }

    /** wordt getriggerd wanneer een speler op een kaartje klikt, zorgt ervoor dat de andere speler ook het kaartje zal
     *  omdraaien door het commando in z'n inbox te laten verschijnen, die 2e speler pullt dan het commando en executet
     *  het
     *
     * @param commando : "FLIP" + uniqueTileId
     * @param activeUser : de actieve user die het triggerd
     * @param currentGameId : voor het huidige spelletje (als hij er meerdere heeft lopen)
     * @throws RemoteException
     */
    @Override
    public void executeFlipCommando(Commando commando, String activeUser, int currentGameId) throws RemoteException {

        getGameSate(currentGameId).executeCommando(commando,activeUser);
    }

    @Override
    public List<Commando> getInbox(String userName, int currentGameId) throws RemoteException{

       // System.out.println("AppServiceImpl : getInbox: door user: "+userName);
        return getGameSate(currentGameId).getInbox(userName);



    }
    //analoog aan https://github.com/aaronhallaert/DS_ChatRMI/blob/master/src/Server/ChatServiceImpl.java



    /**
     * vraag aan db om een bytestream die een image voorstelt te geven
     * @param naam de id van de image
     * @return de image in een array van bytes
     * @throws RemoteException
     */
    @Override
    public byte[] getImage(String naam) throws RemoteException {
        byte[] afbeelding = databaseImpl.getImage(naam);
        return afbeelding;
    }


    /**
     * vraag aan db om een bytestream die een image voorstelt te storen
     * @param naam de id van de image
     * @param afbeelding de image in een array van bytes
     * @throws RemoteException
     */
    @Override
    public void storeImage(String naam, byte[] afbeelding) throws RemoteException{
        databaseImpl.storeImage(naam,afbeelding);
    }

}
