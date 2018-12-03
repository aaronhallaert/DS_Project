import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class Dispatcher {
    public static List<Integer> appServerPoorten=new ArrayList<>();
    public static List<Integer> aantalUsersPerAppServer = new ArrayList<>();

    public static void main(String[] args) {

        try {
            // setup registry met service op poort (zie program arguments) 1902
            System.out.println("dispatcher gelaunced op poort: "+args[0]);
            int appserverPoort=Integer.parseInt(args[1]);
            System.out.println("appserver connected on port "+args[1]);
            appServerPoorten.add(appserverPoort);

/*
            // test opstarten van een tweede applicationserver
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("java -jar \"D:\\School\\Ind Ing\\iiw Master\\Semester 1\\Gedistribueerde Systemen\\Project\\out\\artifacts\\ApplicationServer_jar\\ApplicationServer.jar\" 1905 1901");

            appServerPoorten.add(1905);

*/
            Registry dispatchRegistry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
            dispatchRegistry.rebind("DispatchService", new DispatchImpl());
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
