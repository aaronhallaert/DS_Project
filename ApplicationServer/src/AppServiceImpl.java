import Classes.*;
import SupportiveThreads.GameInfoListReceiver;
import interfaces.AppServerInterface;
import interfaces.DatabaseInterface;
import interfaces.DispatchInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class AppServiceImpl extends UnicastRemoteObject implements AppServerInterface {

    /*--------------- CONNECTIONS ---------------------*/
    private DatabaseInterface databaseImpl; //een willekeurige waarvan we reads gaan doen
    private DatabaseInterface masterDatabaseImpl; // de database via welke de writes gebeuren

    private DispatchInterface dispatchImpl;

    /*--------------- ATTRIBUTES ----------------------*/
    private ArrayList<Game> gamesLijst = new ArrayList<>(); //game bevat GameInfo en GameState
    public ArrayList<GameInfo> gameInfos = new ArrayList<>();

    private BackupGames backup;
    private AppServerInterface destinationBackup;

    private static Map<String, byte[]> imageCache = new HashMap<>();
    private static LinkedList<String> imageCacheSequence = new LinkedList<>();


    GameInfoListReceiver gilr;

    /*-------------- CONSTRUCTOR ----------------------*/
    public AppServiceImpl() throws RemoteException {
        try {

            /*---------- DISPATCH CONN ---------------*/
            dispatchImpl = (DispatchInterface) LocateRegistry.getRegistry("localhost", 1902).lookup("DispatchService");

            /*----------DATABASE CONN ----------------*/
            int databaseServerPoort = getWillekeurigeDatabaseServerPoort();
            System.out.println("connecting with DBServer on port " + databaseServerPoort);
            Registry dataRegistry = LocateRegistry.getRegistry("localhost", databaseServerPoort);
            databaseImpl = (DatabaseInterface) dataRegistry.lookup("DatabaseService");

            //connection met masterdatabase
            if(databaseServerPoort != 1940){
                Registry masterDataRegistry = LocateRegistry.getRegistry("localhost", 1940);
                masterDatabaseImpl = (DatabaseInterface) dataRegistry.lookup("DatabaseService");
            }
            else{
                masterDatabaseImpl = databaseImpl;
            }
            System.out.println("succesvol verbonden met masterDatabasePoort");

            // alle game infos uit database halen + thread voor updates van gameinfo opstarten //
            gameInfos.addAll(databaseImpl.getGameInfoList());
            gilr = new GameInfoListReceiver(this, databaseImpl, gameInfos);
            gilr.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /*-------------- OWN METHODS ----------------------*/
    private int getWillekeurigeDatabaseServerPoort() {

        int willekeurigGetal = (int) (Math.random() * 2 + 1); // willekeurig getal tussen 1 en 3
        switch (willekeurigGetal) {
            case 1:
                return 1940;

            case 2:
                return 1950;

            case 3:
                return 1960;

            default:
                return 1940;
        }
    }

    private synchronized void updateScores(String username, int roosterSize, int eindScore, String command) throws RemoteException {
        masterDatabaseImpl.updateScores(username, roosterSize, eindScore, command, true);
    }

    private HashMap<String, String> generateResultEndGame(HashMap<String, Integer> puntenLijst) {

        HashMap<String, String> returnHm = new HashMap<>();

        ArrayList<String> winners= new ArrayList<>();

        int max= Collections.max(puntenLijst.values());

        for (String speler : puntenLijst.keySet()) {
            int punt= puntenLijst.get(speler);
            if(punt == max){
                winners.add(speler);
            }
        }

        if(winners.size()==1){
            String winner= winners.get(0);

            for (String speler : puntenLijst.keySet()) {
                if(!speler.equals(winner)){
                    returnHm.put(speler, "LOSS");
                }
                else{
                    returnHm.put(speler, "WIN");
                }
            }

        }
        else{
            for (String speler : puntenLijst.keySet()) {
                if (!winners.contains(speler)) {
                    returnHm.put(speler, "LOSS");
                }
            }
            for (String winner : winners) {
                returnHm.put(winner, "DRAW");

            }
        }


        return returnHm;
    }

    /**
     * @param gamesLijst, de te checken lijst van games
     * @param gameId
     * @return true als er al een game in deze lijst zit met deze gameId,
     * false als er nog geen game in de lijst zit
     */
    private boolean reedsGameMetDezeID(ArrayList<Game> gamesLijst, int gameId) {

        for (Game game : gamesLijst) {
            if (game.getGameInfo().getGameId() == gameId) {
                return true;
            }
        }
        return false;

    }

    private boolean vergelijkGameInfoList(List<GameInfo> oudeList, List<GameInfo> gameInfoList){
        if(oudeList.size()!=gameInfoList.size()){
            return false;
        }
        else{
            for (GameInfo gameInfo : gameInfoList) {
                GameInfo foundGameInfo = null;

                for (GameInfo info : oudeList) {
                    if(info.getGameId()==gameInfo.getGameId()){
                        foundGameInfo =info;

                        if(info.getSpelers().size()!=gameInfo.getSpelers().size()){
                            return false;
                        }

                        break;
                    }
                }
                if(foundGameInfo == null){
                    return false;
                }
            }
        }

        return true;
    }
    /*--------------- SERVICES ------------------------*/
    // APPSERVERINFO //
    @Override
    public int getPortNumber() throws RemoteException {
        return AppServerMain.thisappServerpoort;
    }

    @Override
    public boolean testConnection() {
        return true;
    }

    @Override
    public void close() throws RemoteException {
        // TODO check of dit klopt




        ArrayList<Game> toDelete= new ArrayList<>(gamesLijst);
        for (Game game : toDelete) {
            dispatchImpl.changeGameServer(this, game);

            // het probleem is dat de dispatcher games zal verwijderen van deze appserver terwijl dat ie ier aant itereren is erover
        }


        System.exit(0);
    }

    // USER MANAGER //

    /**
     * Deze methode checkt of credentials juist zijn, indien true, aanmaken van token
     *
     * @param username username
     * @param paswoord plain text
     * @return AppServerInterface definieert de connectie tussen deze client en appserver
     * @throws RemoteException
     */
    @Override
    public boolean loginUser(String username, String paswoord) throws RemoteException {

        //als credentials juist zijn
        if (databaseImpl.checkUserCred(username, paswoord)) {

            //maak nieuwe token voor deze persoon aan
            masterDatabaseImpl.createToken(username, paswoord, true);
            return true;
        } else {
            return false;
        }
    }
    /**
     * inloggen met token
     *
     * @param token
     * @param username
     * @return
     * @throws RemoteException
     */
    @Override
    public boolean loginWithToken(String token, String username) throws RemoteException {
        if (databaseImpl.isTokenValid(username, token)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * token timestamp op 0 zetten in db
     *
     * @param username naam van user die uitgelogd wordt
     * @throws RemoteException
     */
    @Override
    public void logoutUser(String username) throws RemoteException {
        masterDatabaseImpl.cancelToken(username, true);
    }

    /**
     * opvragen van token
     *
     * @param username
     * @return
     * @throws RemoteException
     */
    @Override
    public String getToken(String username) throws RemoteException {
        return databaseImpl.getToken(username);
    }


    // GAME INFO //

    @Override
    public boolean prepareForNewGame() throws RemoteException {
        if(gamesLijst.size()>=3){
            return false;
        }

        return true;
    }

    @Override
    public void addGameInBackup(Game game) throws RemoteException {
        backup.getGameList().add(game);
    }

    @Override
    public AppServerInterface getDestinationBackup() throws RemoteException {
        return destinationBackup;
    }

    @Override
    public synchronized int createGame(String activeUser, int dimensies, char set, int aantalSpelers) throws RemoteException {
        //gameId maken, kijken als nog niet reeds bestaat
        int gameId = (int) (Math.random() * 1000);
        while (reedsGameMetDezeID(gamesLijst, gameId)) {
            gameId = (int) (Math.random() * 1000);
        }

        Game game = new Game(gameId, activeUser, dimensies, set, aantalSpelers, AppServerMain.thisappServerpoort);
        gamesLijst.add(game);


        //todo: remove the printing of the kaartjes
        game.getGameState().printSequence();

        System.out.println("game door " + activeUser + " aangemaakt!");
        System.out.println("gameslist grootte is nu: " + gamesLijst.size());

        // game info doorgeven aan database
        masterDatabaseImpl.addGameInfo(game.getGameInfo(), true);

        if(destinationBackup!=null){
            destinationBackup.addGameInBackup(game);
        }

        return gameId;

    }



    @Override
    public int getNumberOfGames() throws RemoteException {
        return gamesLijst.size();
    }

    @Override
    public synchronized ArrayList<GameInfo> getGameInfoLijst(boolean dummy) throws RemoteException {

        ArrayList<GameInfo> oudeList= new ArrayList<>(gameInfos);
        while (vergelijkGameInfoList(oudeList, gameInfos)) {
            try {
                wait();
                System.out.println("game info lijst werd genotified");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("size van gameinfos" + gameInfos.size());
        return new ArrayList<>(gameInfos);



    }

    @Override
    public ArrayList<GameInfo> getGameInfoLijst() throws RemoteException {


        return new ArrayList<>(gameInfos);

    }

    @Override
    public ArrayList<Game> getGamesLijst() throws RemoteException {
        return gamesLijst;
    }

    @Override
    public Game getGame(int currentGameId) {
        for (Game game : gamesLijst) {
            if (game.getGameId() == currentGameId) {
                return game;
            }
        }
        return null;
    }


    @Override
    public GameInfo getGameInfo(int gameId) throws RemoteException {

        for (Game game : gamesLijst) {
            if (game.getGameId() == gameId) {
                return game.getGameInfo();
            }
        }

        //als er geen game gevonden is met de gameId
        return new GameInfo(); // leeg object, herkennen dan in da spel daar
    }

    @Override
    public GameState getGameState(int gameId) throws RemoteException {

        for (Game game : gamesLijst) {
            if (game.getGameId() == gameId) {
                return game.getGameState();
            }
        }

        System.out.println("getGameState in AppserverImpl, mag niet gebeuren");
        return null;
    }

    @Override
    public boolean hasGame(int gameId) throws RemoteException {
        if (this.getGameInfo(gameId).getAppServerPoort() == AppServerMain.thisappServerpoort) {
            return true;
        } else {
            return false;
        }
    }


    // GAME ACTIONS //
    @Override
    public boolean join(String activeUser, int currentGameIdAttempt) throws RemoteException {

        //aanpassing omdat er ook nog moet gejoined worden in de gameState
        if (this.getGameInfo(currentGameIdAttempt).join(activeUser)) {
            // als je kan joinen bij gameinfo kan je joinen bij gamestate
            this.getGameState(currentGameIdAttempt).join(activeUser);
            masterDatabaseImpl.updateGameInfo(getGameInfo(currentGameIdAttempt), true);

            return true;
        } else {
            System.out.println("joinen loopt fout in appServiceImpl.java");
            return false;
        }

    }

    @Override
    public void leaveGame(int currentGameId, String username) throws RemoteException {
        this.getGameInfo(currentGameId).playerLeaves(username);

    }

    @Override
    public boolean changeInPlayers(int currentGameId, int aantalSpelers) throws RemoteException {
        if (this.getGameInfo(currentGameId).changeInPlayers(aantalSpelers)) {
            //System.out.println("er is verandering in users ontdekt");
            return true;
        } else {
            //System.out.println("er geen verandering in users ontdekt");
            return false;
        }

    }

    @Override
    public boolean changeInTurn(int currentGameId, String userTurn) throws RemoteException {
        return this.getGameState(currentGameId).changeInTurn(userTurn);
    }

    /**
     * wordt getriggerd wanneer een speler op een kaartje klikt, zorgt ervoor dat de andere speler ook het kaartje zal
     * omdraaien door het commando in z'n inbox te laten verschijnen, die 2e speler pullt dan het commando en executet
     * het
     *
     * @param commando      : "FLIP" + uniqueTileId
     * @param activeUser    : de actieve user die het triggerd
     * @param currentGameId : voor het huidige spelletje (als hij er meerdere heeft lopen)
     * @throws RemoteException
     */
    @Override
    public void executeFlipCommando(Commando commando, String activeUser, int currentGameId) throws RemoteException {

        HashMap<String, Boolean> values = getGameState(currentGameId).executeCommando(commando, activeUser);
        // values(0) = backup boolean
        // values(1) = game finished boolean


        if (values.get("BACKUP")){
            if (destinationBackup != null) {
                destinationBackup.updateBackupGS(getGameState(currentGameId));
            }
        }


        if (values.get("DONE")){ // game is klaar

            // update scores naar DB
            GameState thisGameState = getGameState(currentGameId);
            GameInfo thisGameInfo = getGameInfo(currentGameId);

            int roosterSize = thisGameInfo.getRoosterSize();
            HashMap<String, Integer> puntenLijst = thisGameState.getPunten();
            HashMap<String, String> resultatenLijst = generateResultEndGame(puntenLijst);

            // voor elke speler natuurlijk
            for (String speler : thisGameState.getSpelers()) {

                updateScores(speler, roosterSize, puntenLijst.get(speler), resultatenLijst.get(speler));
            }


            // gameinfo en gamestate verwijderen uit databaseServer
            deleteGame(currentGameId, true);

            // game finishen in dispatcher ( checken als een AS moet afgesloten worden)
            try {
                dispatchImpl.gameFinished();
            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }

    }



    @Override
    public List<Commando> getInbox(String userName, int currentGameId) throws RemoteException {

        // System.out.println("AppServiceImpl : getInbox: door user: "+userName);
        return getGameState(currentGameId).getInbox(userName);

    }

    @Override
    public void spectate(int gameId, String username) throws RemoteException {
        getGame(gameId).getGameState().getSpectators().add(username);
    }

    @Override
    public void unsubscribeSpecator(int gameId, String username) throws RemoteException {
        getGame(gameId).getGameState().getSpectators().remove(username);
        getGame(gameId).getGameState().getInboxSpectators().remove(username);
    }


    // ASK / PUSH METADATA //


    /**
     * vraag aan db om een bytestream die een image voorstelt te geven
     *
     * @param naam de id van de image
     * @return de image in een array van bytes
     * @throws RemoteException
     */
    @Override
    public byte[] getImage(String naam) throws RemoteException {

        byte[] afbeelding = imageCache.get(naam);

        if (afbeelding == null) {
            afbeelding = databaseImpl.getImage(naam);
            imageCache.put(naam, afbeelding);
            imageCacheSequence.add(naam);
        } else {

        }

        if (imageCache.size() > 36) {
            String removeImage = imageCacheSequence.removeFirst();
            imageCache.remove(removeImage);
        }
        return afbeelding;
    }

    /**
     * vraag aan db om een bytestream die een image voorstelt te storen
     *
     * @param naam       de id van de image
     * @param afbeelding de image in een array van bytes
     * @throws RemoteException
     */
    @Override
    public void storeImage(String naam, byte[] afbeelding) throws RemoteException {
        masterDatabaseImpl.storeImage(naam, afbeelding, true);
    }


    // HANDLING GAMES INTERNALLY //

    /**
     * de gameInfo moet overal verwijderd worden.
     * op deze appserver
     * op de backupAppserver
     * op de databaszq
     * de gameState moet op deze appserver en zijn backup verwijderd worden
     * enkel op deze appserver
     * en in de backupappserver
     *
     * eerst de gameState verwijderen
     *
     * eerst de backupGameState verwijderen
     *
     * @param gameId de id van de game
     * @throws RemoteException
     */
    @Override
    public void deleteGame(int gameId, boolean replicate) throws RemoteException {

        //eerst in de backup hiervan gaan verwijderen
        if (replicate && destinationBackup!=null) {
            destinationBackup.deleteBackupGame(gameId); // zowel gameInfo als GameState
        }

        removeGameFromRunningGames(getGame(gameId));

        // de gameInfo in de databases verwijderen
        if (replicate) {
            masterDatabaseImpl.deleteGameInfo(gameId, true);
        }

        System.out.println("klaar met gameInfo " + gameId + " te verwijderen op DB's");

    }


    @Override
    public synchronized void removeGameFromRunningGames(Game game) throws RemoteException {
        //dan pas lokaal de gameState verijwderen
        int gameId= game.getGameId();
        gamesLijst.remove(game);
        System.out.println("gameState met id: " + gameId + " succesvol verwijderd op appserver");
        //nu de gameState lokaal verwijderen

        // MOET GEDAAN WORDEN DOOR GAMEINFOLISTRECEIVER
        //GameInfo gameInfo = getGameInfo(gameId);
        //gameInfos.remove(gameInfo);
        //System.out.println("gameInfo met id: " + gameId + " succesvol verwijderd op appserver");

    }

    @Override
    public void deleteBackupGame(int gameId) throws RemoteException {
        backup.getGameList().remove(backup.getGame(gameId));
    }




    @Override
    public void takeOverGame(Game game) throws RemoteException {
        game.getGameInfo().setAppServerPoort(AppServerMain.thisappServerpoort);
        masterDatabaseImpl.updateGameInfo(game.getGameInfo(), true);
        gamesLijst.add(game);
    }


    // HANDLING SCORE TABLE //

    @Override
    public void checkIfHasScoreRowAndAddOneIfHasnt(String username) throws RemoteException {

        if (!databaseImpl.hasScoreRij(username)) {

            System.out.println("deze user had nog geen rij in de database");
            masterDatabaseImpl.insertScoreRow(username, true);
            System.out.println("nu wel");
        }

    }

    @Override
    public ArrayList<Score> getScores() throws RemoteException {
        return databaseImpl.getScores();
    }

    // BACKUP //
    @Override
    public void takeBackupFrom(int appserverpoort) throws RemoteException {
        backup = new BackupGames(appserverpoort);

        System.out.println("I just took a backup from " + backup.getAppserverPoort());
        for (Game game : backup.getGameList()) {
            System.out.println(game);
        }

    }

    @Override
    public void setDestinationBackup(int appserverBackupPoort) throws RemoteException {
        if(appserverBackupPoort!=0){
            try {
                destinationBackup = (AppServerInterface) LocateRegistry.getRegistry("localhost", appserverBackupPoort).lookup("AppserverService");
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        }else{
            destinationBackup=null;
        }
    }

    @Override
    public void updateBackupGS(GameState gameState) throws RemoteException {
        //if (!backup.getGame(gameState.getGameId()).getGameState().getAandeBeurt().equals(gameState.getAandeBeurt())) {
            backup.getGame(gameState.getGameId()).setGameState(gameState);


            System.out.println("backup UPGEDATE");
            for (Game game : backup.getGameList()) {
                System.out.println(game);
            }
       // }
    }

    @Override
    public BackupGames getBackup() throws RemoteException {
        return backup;
    }

    @Override
    public synchronized void notifyGameInfoList() throws RemoteException {
        notifyAll();
    }



}
