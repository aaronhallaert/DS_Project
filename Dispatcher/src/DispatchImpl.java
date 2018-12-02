
import SupportiveThreads.ApplicationServerMaintainer;
import interfaces.AppServerInterface;
import interfaces.DatabaseInterface;
import interfaces.DispatchInterface;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class DispatchImpl extends UnicastRemoteObject implements DispatchInterface {

    // TODO: dit moet lijst van databaseInterfaces worden
    DatabaseInterface databaseImpl;

    private ApplicationServerMaintainer asm;

    private int aantalGamesBezig;

    public DispatchImpl() throws RemoteException{
        try {
            // setup communicatie met databaseserver
            // fire to localhost port 1901
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1901);
            // search for application service
            databaseImpl = (DatabaseInterface) myRegistry.lookup("DatabaseService");
        }
        catch(Exception e){
            e.printStackTrace();
        }

        aantalGamesBezig = 0;

        asm = new ApplicationServerMaintainer();
        asm.start();



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
     * Deze methode checkt of credentials juist zijn, indien true, aanmaken van token
     * @param username username
     * @param paswoord plain text
     * @return AppServerInterface definieert de connectie tussen deze client en appserver
     * @throws RemoteException
     */
    @Override
    public AppServerInterface loginUser(String username, String paswoord) throws RemoteException{

        //als credentials juist zijn
        if(databaseImpl.checkUserCred(username, paswoord)){

            //maak nieuwe token voor deze persoon aan
            databaseImpl.createToken(username, paswoord);

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
        return databaseImpl.userNameExists(username);
    }


    /**
     * vraag aan databank om nieuwe user aan te maken met attributen zie parameters
     * @param username
     * @param confirmPassword plain text
     * @throws RemoteException
     */
    @Override
    public void insertUser(String username, String confirmPassword) throws RemoteException {
        databaseImpl.insertUser(username, confirmPassword);
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


    /**
     * inloggen met token
     * @param token
     * @param username
     * @return
     * @throws RemoteException
     */
    @Override
    public AppServerInterface loginWithToken(String token, String username) throws RemoteException {
        if(databaseImpl.isTokenValid(username, token)){
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
            int nieuwPoortNummer = Dispatcher.appServerPoorten.get(Dispatcher.appServerPoorten.size()-1) + 4;

            int databasePoortNummer = 1901;
            //opstarten zelf met dit poortnummer
            //voorlopig 1091 als databasepoort, moet ook dynamisch gekozen worden


            try {
                Runtime rt1 = Runtime.getRuntime();
                rt1.exec("cmd /c start cmd.exe /K \"cd Global && cd jars && java -jar ApplicationServer.jar "+nieuwPoortNummer+" "+databasePoortNummer);

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("fout in DispatchImpl: newGameCreated, jar niet kunnen executen");
            }

            System.out.println("nieuwe appserver started on port "+ nieuwPoortNummer);
            //toevoegen aan de lijst met appServerPoorten
            Dispatcher.appServerPoorten.add(nieuwPoortNummer);

        }
        System.out.println("aantalGamesBezig is nu: "+aantalGamesBezig);

        //todo: hier checken als het aantal een vaste waarde is overschreden, indien ja, start een 2e applicationserver
        // misschien toch in een andere thread dan wel
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
