package client;

import interfaces.AppServerInterface;
import interfaces.DispatchInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Connections {

    Registry dispatchRegistry;
    Registry appRegistry;

    AppServerInterface appImpl;
    DispatchInterface dispatchImpl;

    public Connections(int appserverpoort, int dispatcherpoort ) {

        try {
            dispatchRegistry = LocateRegistry.getRegistry("localhost", dispatcherpoort);
            appRegistry = LocateRegistry.getRegistry("localhost", appserverpoort);

            appImpl=(AppServerInterface) appRegistry.lookup("AppserverService");
            dispatchImpl=(DispatchInterface) dispatchRegistry.lookup("DispatchService");

        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    public AppServerInterface getAppImpl() {
        return appImpl;
    }

    public void setAppImpl(AppServerInterface appImpl) {
        this.appImpl = appImpl;
    }

    public DispatchInterface getDispatchImpl() {
        return dispatchImpl;
    }

    public void setDispatchImpl(DispatchInterface dispatchImpl) {
        this.dispatchImpl = dispatchImpl;
    }
}
