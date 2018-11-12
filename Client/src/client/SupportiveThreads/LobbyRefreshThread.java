package client.SupportiveThreads;

import Classes.GameInfo;
import client.Controllers.LobbyScreen;
import client.Main;

import java.rmi.RemoteException;
import java.util.ArrayList;

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

                if(!equalszelf(serverList,LobbyScreen.gameInfoList)) {
                    //methode in de main, wrapt van ArrayList<Game> -> ObservableList<GameObs>
                    LobbyScreen.gamesObsList = Main.configureList(serverList);
                    LobbyScreen.gameInfoList = new ArrayList<GameInfo>(serverList);
                    System.out.println("refresh");

                    //System.out.println("wait na refresh in lrt");
                    wait(5000);
                    //System.out.println("wait na refresh buiten lrt");
                    ls.refresh();
                }

                else{
                    //System.out.println("wait binnen in lrt");
                    wait(5000);
                    //System.out.println("wait buiten in lrt");
                }
            }
        }
        catch (RemoteException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private boolean equalszelf(ArrayList<GameInfo> serverList, ArrayList<GameInfo> gameInfoList) {

        if(serverList.size() != gameInfoList.size() ){return false;}

        return true;
    }
}
