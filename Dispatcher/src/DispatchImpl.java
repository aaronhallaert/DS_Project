
import SupportiveThreads.ApplicationServerMaintainer;
import interfaces.AppServerInterface;
import interfaces.DatabaseInterface;
import interfaces.DispatchInterface;
import sun.rmi.registry.RegistryImpl_Stub;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class DispatchImpl extends UnicastRemoteObject implements DispatchInterface {

    // TODO: dit moet lijst van databaseInterfaces worden

    ArrayList<DatabaseInterface> dbImpls;
    ArrayList<Registry> dbRegistries;



    private ApplicationServerMaintainer asm;

    private int aantalGamesBezig;

    public DispatchImpl() throws RemoteException{
        try {

            //dbImpls = new ArrayList<DatabaseInterface>();
            //dbRegistries = new ArrayList<Registry>();
            // setup communicatie met databaseserver
            // fire to localhost port 1940 and search for application service
            setupConnectionsToDBs();



            /*
            Registry db1Registry = LocateRegistry.getRegistry("localhost", 1940);
            db1Impl = (DatabaseInterface) db1Registry.lookup("DatabaseService");
            System.out.println("connected with  db on port 1940");

            Registry db2Registry = LocateRegistry.getRegistry("localhost", 1950);
            db2Impl = (DatabaseInterface) db2Registry.lookup("DatabaseService");
            System.out.println("connected with  db on port 1950");

            Registry db3Registry = LocateRegistry.getRegistry("localhost", 1960);
            db3Impl = (DatabaseInterface) db3Registry.lookup("DatabaseService");
            System.out.println("connected with  db on port 1960");

            Registry db4Registry = LocateRegistry.getRegistry("localhost", 1970);
            db4Impl = (DatabaseInterface) db4Registry.lookup("DatabaseService");
            System.out.println("connected with  db on port 1970");

            db1Impl.connectToOtherDbs();
            db3Impl.connectToOtherDbs();
            db2Impl.connectToOtherDbs();
            db4Impl.connectToOtherDbs();*/
            /*for (DatabaseInterface dbImpl : dbImpls) {
                dbImpl.connectToOtherDbs();
            }*/
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



            /*dbRegistries.add((Registry) LocateRegistry.getRegistry("localhost", portnumber));
            DatabaseInterface dbImpl = (DatabaseInterface) dbRegistries.get(i).lookup("DatabaseInterface") ;
            dbImpls.add( dbImpl );
            System.out.println("connected with  db on port " + portnumber);
            portnumber +=10;*/
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

    @Override
    public void newGameCreated() throws RemoteException {
        aantalGamesBezig++;
        int result = asm.setAantalGames(aantalGamesBezig);

        if(result == 1){

            //todo: start nieuwe appserver
            System.out.println("starting nieuwe appserver...");

            // poortnummer van laatst opgestartte appserver nemen en +4 doen
            int applicationPoortNr = Dispatcher.appServerPoorten.get(Dispatcher.appServerPoorten.size()-1) + 4;
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

        }

        System.out.println("aantalGamesBezig is nu: "+aantalGamesBezig);

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
     * @return
     * @throws RemoteException
     */
    private AppServerInterface setupConnectionWithAppImpl() throws RemoteException{
        try {
            // TODO hier moet een keuze gemaakt worden tussen mogelijke actieve appservers
            Registry appRegistry = LocateRegistry.getRegistry("localhost", Dispatcher.appServerPoorten.get(0));
            AppServerInterface appImpl = (AppServerInterface) appRegistry.lookup("AppserverService");
            return appImpl;
        }
        catch(NotBoundException ne){
            // service is niet aanwezig
            ne.printStackTrace();
            return null;
        }
    }




}
