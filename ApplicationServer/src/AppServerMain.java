import interfaces.DatabaseInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class AppServerMain {

    public static int thisappServerpoort;

    public static void main(String[] args) {
        try{
            // setup service op poort 1900
            System.out.println("appserver gelaunched op: "+args[0]);
            thisappServerpoort=Integer.parseInt(args[0]);
            // aanbieden van service
            Registry appRegistry= LocateRegistry.createRegistry(thisappServerpoort);
            appRegistry.rebind("AppserverService", new AppServiceImpl());

            // setup communicatie met databaseserver

        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


}
