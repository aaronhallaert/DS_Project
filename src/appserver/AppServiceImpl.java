package appserver;

import dataserver.DatabaseInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class AppServiceImpl extends UnicastRemoteObject implements AppServerInterface {

    private DatabaseInterface databaseImpl;

    public AppServiceImpl() throws RemoteException{
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
    public void receiveHelloWorld(String test) throws RemoteException {
        System.out.println(test);
    }

    @Override
    public boolean loginUser(String naam, String paswoord) throws RemoteException{
        // connectie met database om in te loggen

        return databaseImpl.checkUser(naam, paswoord);
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
