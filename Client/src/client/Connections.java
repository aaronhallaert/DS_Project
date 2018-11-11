package client;

import interfaces.AppServerInterface;
import interfaces.DispatchInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Connections {

    public Registry dispatchRegistry;

    public AppServerInterface appImpl;
    public DispatchInterface dispatchImpl;

    public Connections(int dispatcherpoort ) {

        try {
            dispatchRegistry = LocateRegistry.getRegistry("localhost", dispatcherpoort);
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
