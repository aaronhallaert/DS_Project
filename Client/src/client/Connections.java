package client;

import interfaces.AppServerInterface;
import interfaces.DispatchInterface;
import javafx.scene.Scene;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Connections {

    private Registry dispatchRegistry;

    public AppServerInterface appImpl;
    private DispatchInterface dispatchImpl;

    public Connections(int dispatcherpoort, Scene close) {
        try {
            dispatchRegistry = LocateRegistry.getRegistry("localhost", dispatcherpoort);
            dispatchImpl=(DispatchInterface) dispatchRegistry.lookup("DispatchService");

            appImpl=dispatchImpl.giveAppserver();
            System.out.println("gekregen appserver heeft als poortnummer " + appImpl.getPortNumber());
        }
        catch(Exception e){
            Main.goToDisconnection(close);
        }

    }

    public AppServerInterface getAppImpl() {
        return appImpl;
    }

    public void setAppImpl(AppServerInterface appImpl) {
        try {
            System.out.println("nieuwe appserver met als poortnummer" + appImpl.getPortNumber());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.appImpl = appImpl;
    }

    public DispatchInterface getDispatchImpl() {
        return dispatchImpl;
    }
}
