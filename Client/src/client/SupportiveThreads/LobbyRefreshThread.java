package client.SupportiveThreads;

import Classes.GameInfo;
import client.Controllers.LobbyScreen;
import client.Main;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * deze thread zorgt ervoor dat er om de 5 seconden de tabel in het lobbyscreen gereset wordt
 */
public class LobbyRefreshThread extends Thread{

    private LobbyScreen ls;

    public LobbyRefreshThread(LobbyScreen lobbyScreen) {
        ls = lobbyScreen;
    }

    @Override
    public synchronized void run() {
        super.run();

        try {
            while (true) {

                ArrayList<GameInfo> serverList= Main.cnts.getAppImpl().getGameInfoLijst();

                    //methode in de main, wrapt van ArrayList<Game> -> ObservableList<GameObs>
                    LobbyScreen.gamesObsList = Main.configureList(serverList);
                    LobbyScreen.gameInfoList = new ArrayList<GameInfo>(serverList);
                    System.out.println("refresh");

                    //System.out.println("wait na refresh in lrt");
                    wait(5000);
                    //System.out.println("wait na refresh buiten lrt");
                    ls.refresh();
            }
        }
        catch (RemoteException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
