import Classes.*;
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
    private DatabaseInterface databaseImpl;
    private DispatchInterface dispatchImpl;

    /*--------------- ATTRIBUTES ----------------------*/
    private ArrayList<Game> gamesLijst=new ArrayList<>(); //game bevat GameInfo en GameState

    private BackupGames backup;
    private AppServerInterface destinationBackup;

    private static Map<String, byte[]> imageCache=new HashMap<>();
    private static LinkedList<String> imageCacheSequence= new LinkedList<>();

    /*-------------- CONSTRUCTOR ----------------------*/
    public AppServiceImpl() throws RemoteException{
        try {

            // setup communicatie met databaseserver
            // fire to localhost port 1900
            //TODO verbind naar willekeurige db
            // todo: dit moet dynamisch 1940,1950 of 1960 worden
            int databaseServerPoort = getWillekeurigeDatabaseServerPoort();
            System.out.println("connecting with DBServer on port "+ databaseServerPoort);
            Registry dataRegistry= LocateRegistry.getRegistry("localhost",databaseServerPoort);
            // search for database service
            databaseImpl=(DatabaseInterface) dataRegistry.lookup("DatabaseService");



            //dispatcher
            Registry dispatchReg= LocateRegistry.getRegistry("localhost", 1902);
            dispatchImpl=(DispatchInterface) dispatchReg.lookup("DispatchService");

        }
        catch(Exception e){
            e.printStackTrace();
        }

        //gamesLijst = new ArrayList<>();
        System.out.println("gamesList created");

    }


    /*-------------- OWN METHODS ----------------------*/
    private int getWillekeurigeDatabaseServerPoort() {

        int willekeurigGetal = (int)(Math.random() *2 +1); // willekeurig getal tussen 1 en 3
        switch(willekeurigGetal){
            case 1: return 1940;

            case 2: return 1950;

            case 3: return 1960;

            default: return 1940;
        }
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
    // USER MANAGER //
    /**
     * Deze methode checkt of credentials juist zijn, indien true, aanmaken van token
     * @param username username
     * @param paswoord plain text
     * @return AppServerInterface definieert de connectie tussen deze client en appserver
     * @throws RemoteException
     */
    @Override
    public boolean loginUser(String username, String paswoord) throws RemoteException{

        //als credentials juist zijn
        if(databaseImpl.checkUserCred(username, paswoord)){

            //maak nieuwe token voor deze persoon aan
            databaseImpl.createToken(username, paswoord);
            return true;
        }
        else{
            return false;
        }
    }
    /**
     * inloggen met token
     * @param token
     * @param username
     * @return
     * @throws RemoteException
     */
    @Override
    public boolean loginWithToken(String token, String username) throws RemoteException {
        if(databaseImpl.isTokenValid(username, token)){
            return true;
        }
        else{
            return false;
        }
    }
    /**
     * token timestamp op 0 zetten in db
     * @param username naam van user die uitgelogd wordt
     * @throws RemoteException
     */
    @Override
    public void logoutUser(String username) throws RemoteException{
        databaseImpl.cancelToken(username);

    }
    /**
     * opvragen van token
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
    public synchronized int createGame(String activeUser, int dimensies, char set, int aantalSpelers) throws RemoteException {


        System.out.println(activeUser);

        //gameId maken, kijken als nog niet reeds bestaat
        int gameId = (int)(Math.random() * 1000);
        while(reedsGameMetDezeID(gamesLijst,gameId)){
            gameId = (int)(Math.random() * 1000);
        }

        Game game = new Game(gameId ,activeUser, dimensies, set, aantalSpelers, AppServerMain.thisappServerpoort);
        gamesLijst.add(game);
        System.out.println("game met naam "+activeUser+" gemaakt!");
        System.out.println("gameslist grootte is nu: "+gamesLijst.size());

        // TODO update database met gameinfo
        databaseImpl.addGameInfo(game.getGameInfo());

        notifyAll();

        return gameId;

    }
    @Override
    public int getNumberOfGames() throws RemoteException {
        return gamesLijst.size();
    }
    /**
     * @return
     * @throws RemoteException
     */
    @Override
    public synchronized ArrayList<GameInfo> getGameInfoLijst(int currentSize) throws RemoteException {


        while(currentSize==gamesLijst.size()){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ArrayList<GameInfo> gameInfoLijst = new ArrayList<GameInfo>();

        for (Game game : gamesLijst) {
            gameInfoLijst.add(game.getGameInfo());
        }

        return gameInfoLijst;

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
    public ArrayList<Game> getGamesLijst() throws RemoteException {
        return gamesLijst;
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
    public GameState getGameState(int gameId) throws RemoteException {

        for (Game game : gamesLijst) {
            if(game.getGameId() == gameId){
                return game.getGameState();
            }
        }

        System.out.println("getGameState in AppserverImpl, mag niet gebeuren");
        return null;
    }
    @Override
    public boolean hasGame(int gameId) throws RemoteException{
        if(this.getGameInfo(gameId).getAppServerPoort()==AppServerMain.thisappServerpoort){
            return true;
        }
        else{
            return false;
        }
    }


    // GAME ACTIONS //
    @Override
    public boolean join(String activeUser, int currentGameIdAttempt) throws RemoteException {

        //aanpassing omdat er ook nog moet gejoined worden in de gameState
        if(this.getGameInfo(currentGameIdAttempt).join(activeUser)){
            // als de if clause lukt,
            // dan zal het getGameState ook lukken, daarom is getGameState een void
            this.getGameState(currentGameIdAttempt).join(activeUser);
            databaseImpl.updateGameInfo(getGameInfo(currentGameIdAttempt));
            //setGameStatenaam nog
            return true;
        }
        else {
            System.out.println("joinen loopt fout in appServiceImpl.java");
            return false;
        }

    }
    @Override
    public void leaveGame(int currentGameId, String username) throws RemoteException {
        this.getGameInfo(currentGameId).playerLeaves(username);

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
    public boolean changeInTurn(int currentGameId, String userTurn) throws RemoteException{
        boolean change=this.getGameState(currentGameId).changeInTurn(userTurn);


        // TODO dit wordt meerdere keren gedaan aangezien de methode "changeInTurn" aangeroepen wordt door alle clients die de game aan het spelen zijn
        if(destinationBackup!=null){
            destinationBackup.updateBackupGS(getGameState(currentGameId));
        }

        return change;
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
        getGameState(currentGameId).executeCommando(commando,activeUser);
    }
    @Override
    public List<Commando> getInbox(String userName, int currentGameId) throws RemoteException{

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
     * @param naam de id van de image
     * @return de image in een array van bytes
     * @throws RemoteException
     */
    @Override
    public byte[] getImage(String naam) throws RemoteException {

        byte[] afbeelding = imageCache.get(naam);

        if(afbeelding==null) {
            afbeelding= databaseImpl.getImage(naam);
            imageCache.put(naam, afbeelding);
            imageCacheSequence.add(naam);
        }
        else{

        }

        if(imageCache.size()>36) {
            String removeImage = imageCacheSequence.removeFirst();
            imageCache.remove(removeImage);
        }
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


    // HANDLING GAMES INTERNALLY //
    @Override
    public void removeGame(Game game) throws RemoteException{
        gamesLijst.remove(game);
    }
    @Override
    public void takeOverGame(Game game) throws RemoteException {
        game.getGameInfo().setAppServerPoort(AppServerMain.thisappServerpoort);
        databaseImpl.updateGameInfo(game.getGameInfo());
        gamesLijst.add(game);
    }


    // HANDLING SCORE TABLE //
    @Override
    public void updateScores(String username, int roosterSize, int eindScore, String command) throws RemoteException {
        databaseImpl.updateScores(username, roosterSize, eindScore, command);

    }
    @Override
    public void checkIfHasScoreRowAndAddOneIfHasnt(String username) throws RemoteException {

        if(!databaseImpl.hasScoreRij(username)){

            System.out.println("deze user had nog geen rij in de database");
            databaseImpl.insertScoreRow(username);
            System.out.println("nu wel");
        }

    }
    @Override
    public ArrayList<Score> getScores() throws RemoteException {
        return databaseImpl.getScores();
    }

    // BACKUP //
    @Override
    public void takeBackupFrom(int appserverpoort) throws RemoteException{
        backup= new BackupGames(appserverpoort);

        System.out.println("I just took a backup from " + backup.getAppserverPoort());
        for (Game game : backup.getGameList()) {
            System.out.println(game);
        }

    }

    @Override
    public void setDestinationBackup(int appserverBackupPoort) throws RemoteException{
        try {
            destinationBackup= (AppServerInterface) LocateRegistry.getRegistry("localhost", appserverBackupPoort).lookup("AppserverService");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateBackupGS(GameState gameState) throws RemoteException {

        //TODO update enkel als het nog niet is upgedate
        if(!backup.getGame(gameState.getGameId()).getGameState().getAandeBeurt().equals(gameState.getAandeBeurt())) {
            backup.getGame(gameState.getGameId()).setGameState(gameState);


            System.out.println("backup UPGEDATE");
            for (Game game : backup.getGameList()) {
                System.out.println(game);
            }
        }
    }
}
