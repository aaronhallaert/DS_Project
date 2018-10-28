package dispatcher;

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
    public AppServerInterface loginUser(String naam, String paswoord) throws RemoteException{

        //als credentials juist zijn, maak nieuwe token voor deze persoon aan
        if(databaseImpl.checkUserCred(naam, paswoord)){
            databaseImpl.createToken(naam, paswoord);

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
}
