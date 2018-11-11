
import interfaces.AppServerInterface;
import interfaces.DatabaseInterface;
import interfaces.DispatchInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class DispatchImpl extends UnicastRemoteObject implements DispatchInterface {
    DatabaseInterface databaseImpl;
    public DispatchImpl() throws RemoteException{
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
    }

    @Override
    public void logoutUser(String username) throws RemoteException{
        databaseImpl.cancelToken(username);

    }

    @Override
    public AppServerInterface loginUser(String naam, String paswoord) throws RemoteException{

        //als credentials juist zijn, maak nieuwe token voor deze persoon aan
        if(databaseImpl.checkUserCred(naam, paswoord)){
            databaseImpl.createToken(naam, paswoord);

            return setupConnectionWithAppImpl();

        }
        return null;
    }

    @Override
    public boolean userNameExists(String username) throws RemoteException {
        return databaseImpl.userNameExists(username);
    }

    @Override
    public void insertUser(String username, String confirmPassword) throws RemoteException {
        databaseImpl.insertUser(username, confirmPassword);
    }

    @Override
    public String getToken(String username) throws RemoteException {
        return databaseImpl.getToken(username);
    }

    @Override
    public AppServerInterface loginWithToken(String token, String username) throws RemoteException {
        if(databaseImpl.isTokenValid(username, token)){
            return setupConnectionWithAppImpl();
        }
        else{
            return null;
        }
    }

    private AppServerInterface setupConnectionWithAppImpl() throws RemoteException{
        try {
            Registry appRegistry = LocateRegistry.getRegistry("localhost", Dispatcher.appserverPoort);
            AppServerInterface appImpl = (AppServerInterface) appRegistry.lookup("AppserverService");
            return appImpl;
        }
        catch(NotBoundException ne){
            ne.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] getImage(String naam) throws RemoteException {

        System.out.println("dispatchImpl: lijn 47: check");
        byte[] afbeelding = databaseImpl.getImage(naam);
        //InputStream returner = databaseImpl.getImage(naam);
        //System.out.println("check check");
        //return returner;
        return afbeelding;
    }

    @Override
    public void storeImage(String naamFoto, byte[] afbeelding) throws RemoteException{

        System.out.println("storeImage triggered");

        databaseImpl.storeImage(naamFoto,afbeelding);


    }







}
