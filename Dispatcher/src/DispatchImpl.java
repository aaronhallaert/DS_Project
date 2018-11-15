
import interfaces.AppServerInterface;
import interfaces.DatabaseInterface;
import interfaces.DispatchInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class DispatchImpl extends UnicastRemoteObject implements DispatchInterface {

    // TODO: dit moet lijst van databaseInterfaces worden
    DatabaseInterface databaseImpl;


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


    /**
     * hier wordt een connectie gemaakt met de appserver
     * @return
     * @throws RemoteException
     */
    private AppServerInterface setupConnectionWithAppImpl() throws RemoteException{
        try {
            // TODO hier moet een keuze gemaakt worden tussen mogelijke actieve appservers
            Registry appRegistry = LocateRegistry.getRegistry("localhost", Dispatcher.appserverPoort);
            AppServerInterface appImpl = (AppServerInterface) appRegistry.lookup("AppserverService");
            return appImpl;
        }
        catch(NotBoundException ne){
            // service is niet aanwezig
            ne.printStackTrace();
            return null;
        }
    }

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
