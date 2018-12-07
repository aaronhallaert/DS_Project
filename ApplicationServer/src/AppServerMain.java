import interfaces.DatabaseInterface;
import interfaces.DispatchInterface;

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


        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


}
