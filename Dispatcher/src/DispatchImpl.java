
import Classes.Game;
import SupportiveThreads.ApplicationServerMaintainer;
import interfaces.AppServerInterface;
import interfaces.DatabaseInterface;
import interfaces.DispatchInterface;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class DispatchImpl extends UnicastRemoteObject implements DispatchInterface {

    //todo: ASM moet niet meer in een thread


    /*------------------ATTRIBUTES ---------------------------------------*/
    private static List<Integer> appServerPoorten=new ArrayList<>();
    private static List<AppServerInterface> appImpls=new ArrayList<>();

    private ArrayList<DatabaseInterface> dbImpls=new ArrayList<>();
    private ApplicationServerMaintainer asm;
    private int aantalGamesBezig;


    /*------------------CONSTRUCTORS -------------------------------------*/
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


    }



    /*------------------ OWN METHODS ----------------------------------------*/
    public void makeNewAppserver(){
        List<Integer> mogelijkPoortnummer= new ArrayList<>();
        int beginPoortnummer=2000;
        for (int i = 0; i <2080 ; i+=4) {
            mogelijkPoortnummer.add(beginPoortnummer+i);
        }

        // poortnummer van laatst opgestartte appserver nemen en +4 doen
        for (int integer : mogelijkPoortnummer) {
            if(!appServerPoorten.contains(integer)){
                // maak maar aan
                int applicationPoortNr= integer;
                try {

                    //start een nieuwe appserver op
                    Runtime rt1 = Runtime.getRuntime();
                    rt1.exec("cmd /c start cmd.exe /K \"cd Global && cd jars && java -jar ApplicationServer.jar "+applicationPoortNr);

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("fout in DispatchImpl: newGameCreated, jar niet kunnen executen");
                }

                //toevoegen aan de lijst met appServerPoorten
                System.out.println("nieuwe appserver started on port "+ applicationPoortNr);
                break;
            }
        }
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

    private void closeAppServer(AppServerInterface appImpl) throws RemoteException{

        if(appImpls.size()>2) {
            try {
                AppServerInterface backupFrom = (AppServerInterface) LocateRegistry.getRegistry("localhost", appImpl.getBackup().getAppserverPoort()).lookup("AppserverService");
                AppServerInterface destinationBackup = appImpl.getDestinationBackup();

                destinationBackup.takeBackupFrom(backupFrom.getPortNumber());
                destinationBackup.setDestinationBackup(destinationBackup.getPortNumber());

            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        }
        else{
            AppServerInterface destinationBackup = appImpl.getDestinationBackup();
            destinationBackup.setDestinationBackup(0);
        }

        appImpl.close();


        int index= appImpls.indexOf(appImpl);
        appImpls.remove(appImpl);
        appServerPoorten.remove(index);
    }

    /**
     * Wanneer een appserver crasht
     * @param appserverImpl
     */
    private void removeAppImpl(AppServerInterface appserverImpl){
        int index= appImpls.indexOf(appserverImpl);
        appImpls.remove(appserverImpl);
        appServerPoorten.remove(index);

        //crash recovery implementatie, hoeven we niet te doen
    }


    /*------------------ SERVICES ZIE INTERFACE------------------------------*/

    // APPSERVER MANAGING //
    @Override
    public void registerAppserver(int portNumber) {
        System.out.println("nieuwe appserver geregistreerd met poortnummer "+ portNumber);
        try {
            AppServerInterface newAppImpl= (AppServerInterface)LocateRegistry.getRegistry("localhost", portNumber).lookup("AppserverService");

            if(appImpls.size()>=1) {
                appImpls.get(0).takeBackupFrom(portNumber);
                newAppImpl.setDestinationBackup(appImpls.get(0).getPortNumber());


                newAppImpl.takeBackupFrom(appServerPoorten.get(appServerPoorten.size() - 1));
                appImpls.get(appImpls.size()-1).setDestinationBackup(newAppImpl.getPortNumber());
            }


            appImpls.add(newAppImpl);
            appServerPoorten.add(portNumber);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }




    }

    @Override
    public AppServerInterface giveAppserver() throws RemoteException {
        Random r= new Random();
        int appserverIndex= r.nextInt(appServerPoorten.size());
        AppServerInterface appserverImpl= appImpls.get(appserverIndex);

        try {
            if (appserverImpl.testConnection()) {
                return appserverImpl;
            }
        }
        catch (RemoteException re){
            removeAppImpl(appserverImpl);

            if(appImpls.isEmpty()) {
                makeNewAppserver();
            }
            return null;
        }

        return null;

    }

    @Override
    public void newGameCreated() throws RemoteException {
        aantalGamesBezig++;
        int result = asm.setAantalGames(aantalGamesBezig, appImpls.size());

        if(result == 1){

            System.out.println("starting nieuwe appserver...");
            makeNewAppserver();

        }

        System.out.println("aantalGamesBezig is nu: "+aantalGamesBezig);

    }

    @Override
    public void gameFinished() throws RemoteException{

        aantalGamesBezig--;

        int result = asm.setAantalGames(aantalGamesBezig, appImpls.size());

        if(result == -1){
            for (AppServerInterface appImpl : appImpls) {
                if(appImpl.getNumberOfGames()<3){
                    closeAppServer(appImpl);

                }
            }
        }
        System.out.println("aantalGamesBezig is nu: "+aantalGamesBezig);
    }


    // USER MANAGING //
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
        return dbImpls.get(1).userNameExists(username);
    }
    /**
     * vraag aan databank om nieuwe user aan te maken met attributen zie parameters
     * @param username
     * @param confirmPassword plain text
     * @throws RemoteException
     */
    @Override
    public void insertUser(String username, String confirmPassword) throws RemoteException {
        dbImpls.get(0).insertUser(username, confirmPassword, true); //doet hij op de master
        dbImpls.get(0).insertScoreRow(username, true); // doet hij op de master
    }


    // OVERZETTEN VAN ... //
    @Override
    public synchronized void changeGameServer(AppServerInterface currentAppImpl, Game game) throws RemoteException{
            List<AppServerInterface> possibleAppServers=new ArrayList<>();
            for (AppServerInterface appImpl : appImpls) {
                if(appImpl.getNumberOfGames()<3 && appImpl!=currentAppImpl){
                    possibleAppServers.add(appImpl);
                }
            }

            if(possibleAppServers.size()==0){
                makeNewAppserver();
                changeGameServer(currentAppImpl, game);
            }
            else{
                possibleAppServers.get(0).takeOverGame(game);
                currentAppImpl.removeGameFromRunningGames(game);
            }
    }

    @Override
    public AppServerInterface changeClientServer(int currentGameIdAttempt) throws RemoteException {
        for (AppServerInterface appImpl : appImpls) {
            if(appImpl.hasGame(currentGameIdAttempt)){
                return appImpl;
            }
        }

        System.out.println("Mag niet voorkomen, maar kan wel in theorie");
        return null;
    }

    @Override
    public AppServerInterface changeClientServer(boolean dummy) throws RemoteException {
        for (AppServerInterface appImpl : appImpls) {
            if(appImpl.prepareForNewGame()){
                return appImpl;
            }
        }
        makeNewAppserver();
        return null;
    }


}
