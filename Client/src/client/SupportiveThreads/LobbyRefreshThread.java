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
    public void run() {
        try {
            while (true) {
                // zal enkel returnen als grootte van lijst veranderd is
                ArrayList<GameInfo> serverList= Main.cnts.getAppImpl().getGameInfoLijst(true);

                System.out.println("nieuwe list verkregen");
                // methode in de main, wrapt van ArrayList<Game> -> ObservableList<GameObs>
                LobbyScreen.gamesObsList = Main.configureList(serverList);
                LobbyScreen.gameInfoList = new ArrayList<GameInfo>(serverList);

                // refresh de table
                ls.refresh();
            }
        }
        catch (RemoteException e){
            System.out.println("application server is uitgevallen");
            Main.fixDisconnection(ls.joinErrorLabel.getScene());
        }

        super.run();
    }
}
