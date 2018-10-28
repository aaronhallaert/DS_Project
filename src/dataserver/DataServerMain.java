package dataserver;


import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DataServerMain {

    public static void main(String[] args) {

        //vast poortnummer
        try {
            System.out.println("dataserver gelaunched op: "+ args[0]);
            Registry dataRegistry= LocateRegistry.createRegistry(Integer.parseInt(args[0]));
            dataRegistry.rebind("DatabaseService", new DatabaseImpl());

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }













}
