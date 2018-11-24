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
                System.out.println("vraag naar infolijst");
                ArrayList<GameInfo> serverList= Main.cnts.getAppImpl().getGameInfoLijst(LobbyScreen.gameInfoList.size());

                //methode in de main, wrapt van ArrayList<Game> -> ObservableList<GameObs>
                LobbyScreen.gamesObsList = Main.configureList(serverList);
                LobbyScreen.gameInfoList = new ArrayList<GameInfo>(serverList);
                System.out.println("refresh");

                ls.refresh();
            }
        }
        catch (RemoteException e){
            System.out.println("application server is uitgevallen");
            Main.fixDisconnection(ls.joinErrorLabel.getScene());
        }

    }
}
