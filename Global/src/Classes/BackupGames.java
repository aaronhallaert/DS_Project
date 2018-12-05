package Classes;

import interfaces.AppServerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;


public class BackupGames {

    private int appserverPoort;
    private ArrayList<Game> gameList= new ArrayList<>();

    public BackupGames(int appserverPoot){
        this.appserverPoort=appserverPoot;
        try {
            AppServerInterface appImpl= (AppServerInterface) LocateRegistry.getRegistry("localhost", appserverPoot).lookup("AppserverService");
            gameList= new ArrayList<>(appImpl.getGamesLijst());

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }


    public int getAppserverPoort() {
        return appserverPoort;
    }

    public void setAppserverPoort(int appserverPoort) {
        this.appserverPoort = appserverPoort;
    }

    public ArrayList<Game> getGameList() {
        return gameList;
    }

    public void setGameList(ArrayList<Game> gameList) {
        this.gameList = gameList;
    }
}
