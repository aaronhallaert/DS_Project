import SupportiveThreads.PollToOtherDBs;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DataServerMain {

    public static String databaseNaam;
    public static PollToOtherDBs pollToOtherDBs;

    public static void main(String[] args) {
        // we gaan de databaseServers runnen op poorten 1940,1950,1960,
        try {
            System.out.println("dataserver gelaunched op: "+ args[0]);
            databaseNaam = getDbName(args[0]);

            // service aanbieden
            Registry dataRegistry= LocateRegistry.createRegistry(Integer.parseInt(args[0]));
            dataRegistry.rebind("DatabaseService", new DatabaseImpl(databaseNaam));

            // zoeken naar andere databases
            pollToOtherDBs = new PollToOtherDBs(args[0]);
            pollToOtherDBs.start();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String getDbName(String arg) {
        switch(arg){
            case "1940": return "memorydb.db";
            case "1950": return "database2.db";
            case "1960": return "database3.db";
            case "1970": return "database4.db";
            default:
                System.out.println("probleem in getDbName in DataServerMain.java");
                return "memorydb.db";
        }
    }

}
