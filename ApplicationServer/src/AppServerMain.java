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
            // fire to localhost port 1940 todo: dit moet dynamisch 1940,1950 of 1960 worden
            int databaseServerPoort = getWillekeurigeDatabaseServerPoort();
            System.out.println("connecting with DBServer on port "+ databaseServerPoort);
            Registry dataRegistry= LocateRegistry.getRegistry("localhost",Integer.parseInt(args[1]));
            // search for database service
            DatabaseInterface impl=(DatabaseInterface) dataRegistry.lookup("DatabaseService");



        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private static int getWillekeurigeDatabaseServerPoort() {

        int willekeurigGetal = (int)(Math.random() *2 +1); // willekeurig getal tussen 1 en 3
        switch(willekeurigGetal){
            case 1: return 1940;

            case 2: return 1950;

            case 3: return 1960;

            default: return 1940;
        }
    }
}
