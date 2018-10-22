package dispatcher;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Dispatcher {

    public static void main(String[] args) {

        try {
            // setup registry met service op poort (zie program arguments)
            System.out.println("dispatcher gelaunced op poort: "+args[0]);
            Registry dispatchRegistry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
            dispatchRegistry.rebind("DispatchService", new DispatchImpl());
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
