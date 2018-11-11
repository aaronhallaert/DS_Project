import interfaces.DatabaseInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class AppServerMain {

    public static void main(String[] args) {
        try{
            // setup service op poort 1900
            System.out.println("appserver gelaunched op: "+args[0]);
            Registry appRegistry= LocateRegistry.createRegistry(Integer.parseInt(args[0]));
            appRegistry.rebind("AppserverService", new AppServiceImpl());

            // setup communicatie met databaseserver
            // fire to localhost port 1901
            Registry dataRegistry= LocateRegistry.getRegistry("localhost",Integer.parseInt(args[1]));
            // search for database service
            DatabaseInterface impl=(DatabaseInterface) dataRegistry.lookup("DatabaseService");

        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
