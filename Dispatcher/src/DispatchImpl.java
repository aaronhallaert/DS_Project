
import Classes.Game;
import SupportiveThreads.ApplicationServerMaintainer;
import interfaces.AppServerInterface;
import interfaces.DatabaseInterface;
import interfaces.DispatchInterface;
import sun.rmi.registry.RegistryImpl_Stub;
import sun.util.locale.LocaleExtensions;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DispatchImpl extends UnicastRemoteObject implements DispatchInterface {

    // TODO: dit moet lijst van databaseInterfaces worden

    ArrayList<DatabaseInterface> dbImpls=new ArrayList<>();

    private ApplicationServerMaintainer asm;

    private int aantalGamesBezig;

    public DispatchImpl() throws RemoteException{
        try {


            // setup communicatie met databaseserver
            setupConnectionsToDBs();

        }
        catch(Exception e){
            e.printStackTrace();
        }

        aantalGamesBezig = 0;

        asm = new ApplicationServerMaintainer();
        asm.start();



    }

    private void setupConnectionsToDBs() throws RemoteException, NotBoundException {
        int portnumber = 1940;
        for(int i=0 ; i<4 ; i++){
            DatabaseInterface dbImp= (DatabaseInterface) LocateRegistry.getRegistry("localhost", portnumber).lookup("DatabaseService");
            dbImpls.add(dbImp);
            portnumber+=10;
        }

        for (DatabaseInterface dbImpl : dbImpls) {
            for (DatabaseInterface toImpl : dbImpls) {
                if(dbImpl != toImpl){
                    dbImpl.connectTo(toImpl);
                }
            }
        }
    }


    /**
     * token timestamp op 0 zetten in db
     * @param username naam van user die uitgelogd wordt
     * @throws RemoteException
     */
    @Override
    public void logoutUser(String username) throws RemoteException{
        dbImpls.get(0).cancelToken(username);

    }


    /**
     * Deze methode checkt of credentials juist zijn, indien true, aanmaken van token
     * @param username username
     * @param paswoord plain text
     * @return AppServerInterface definieert de connectie tussen deze client en appserver
     * @throws RemoteException
     */
    @Override
    public AppServerInterface loginUser(String username, String paswoord) throws RemoteException{

        //als credentials juist zijn
        if(dbImpls.get(0).checkUserCred(username, paswoord)){

            //maak nieuwe token voor deze persoon aan
            dbImpls.get(0).createToken(username, paswoord);

            return setupConnectionWithAppImpl();

        }
        return null;
    }


    /**
     * vraag aan databank of username reeds in gebruik is
     * wordt opgeroepen bij registreren
     *
     * @param username
     * @return
     * @throws RemoteException
     */
    @Override
    public boolean userNameExists(String username) throws RemoteException {
        return dbImpls.get(0).userNameExists(username);
    }


    /**
     * vraag aan databank om nieuwe user aan te maken met attributen zie parameters
     * @param username
     * @param confirmPassword plain text
     * @throws RemoteException
     */
    @Override
    public void insertUser(String username, String confirmPassword) throws RemoteException {
        dbImpls.get(0).insertUser(username, confirmPassword);
    }


    /**
     * opvragen van token
     * @param username
     * @return
     * @throws RemoteException
     */
    @Override
    public String getToken(String username) throws RemoteException {
        return dbImpls.get(0).getToken(username);
    }


    /**
     * inloggen met token
     * @param token
     * @param username
     * @return
     * @throws RemoteException
     */
    @Override
    public AppServerInterface loginWithToken(String token, String username) throws RemoteException {
        if(dbImpls.get(0).isTokenValid(username, token)){
            return setupConnectionWithAppImpl();
        }
        else{
            return null;
        }
    }

    public void makeNewAppserver(){
        List<Integer> mogelijkPoortnummer= new ArrayList<>();
        int beginPoortnummer=2000;
        for (int i = 0; i <2080 ; i+=4) {
            mogelijkPoortnummer.add(beginPoortnummer+i);
        }

        // poortnummer van laatst opgestartte appserver nemen en +4 doen
        for (int integer : mogelijkPoortnummer) {
            if(!Dispatcher.appServerPoorten.contains(integer)){
                // maak maar aan
                int applicationPoortNr= integer;
                int databasePoortNr = 1901;
                try {

                    //start een nieuwe appserver op
                    Runtime rt1 = Runtime.getRuntime();
                    rt1.exec("cmd /c start cmd.exe /K \"cd Global && cd jars && java -jar ApplicationServer.jar "+applicationPoortNr+" "+databasePoortNr);

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("fout in DispatchImpl: newGameCreated, jar niet kunnen executen");
                }

                //toevoegen aan de lijst met appServerPoorten
                System.out.println("nieuwe appserver started on port "+ applicationPoortNr);
                Dispatcher.appServerPoorten.add(applicationPoortNr);
                try {
                    AppServerInterface newAppServer=(AppServerInterface) LocateRegistry.getRegistry("localhost", applicationPoortNr).lookup("AppserverService");
                    Dispatcher.appImpls.add(newAppServer);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }


    @Override
    public void newGameCreated() throws RemoteException {
        aantalGamesBezig++;
        int result = asm.setAantalGames(aantalGamesBezig);

        if(result == 1){

            //todo: start nieuwe appserver
            System.out.println("starting nieuwe appserver...");
            makeNewAppserver();

        }

        System.out.println("aantalGamesBezig is nu: "+aantalGamesBezig);

    }


    @Override
    public void changeGameServer(AppServerInterface currentAppImpl, Game game) throws RemoteException{
        List<AppServerInterface> possibleAppServers=new ArrayList<>();
        for (AppServerInterface appImpl : Dispatcher.appImpls) {
            if(appImpl.getNumberOfGames()<3){
                possibleAppServers.add(appImpl);
            }
        }

        if(possibleAppServers.size()==0){
            makeNewAppserver();
            changeGameServer(currentAppImpl, game);
        }
        else{
            possibleAppServers.get(0).takeOverGame(game);
            currentAppImpl.removeGame(game);
        }



    }

    @Override
    public AppServerInterface changeClientServer(int currentGameIdAttempt) throws RemoteException {
        for (AppServerInterface appImpl : Dispatcher.appImpls) {
            if(appImpl.hasGame(currentGameIdAttempt)){
                return appImpl;
            }
        }

        System.out.println("Mag niet voorkomen, maar kan wel in theorie");
        return null;
    }


    @Override
    public void gameFinished() throws RemoteException{
        aantalGamesBezig--;
        int result = asm.setAantalGames(aantalGamesBezig);
        if(result == -1){
            //todo: stop een appserver?
        }
        System.out.println("aantalGamesBezig is nu: "+aantalGamesBezig);
    }


    /**
     * hier wordt een connectie gemaakt met de appserver
     *
     * @return
     * @throws RemoteException
     */
    private AppServerInterface setupConnectionWithAppImpl() throws RemoteException{
        try {
            // TODO hier moet een keuze gemaakt worden tussen mogelijke actieve appservers
            if(Dispatcher.appImpls.size()==0){
                Registry appRegistry = LocateRegistry.getRegistry("localhost", Dispatcher.appServerPoorten.get(0));
                AppServerInterface appImpl = (AppServerInterface) appRegistry.lookup("AppserverService");
                Dispatcher.appImpls.add(appImpl);
                return appImpl;
            }
            else{
                return Dispatcher.appImpls.get(0);
            }

        }
        catch(NotBoundException ne){
            // service is niet aanwezig
            ne.printStackTrace();
            return null;
        }
    }





}
