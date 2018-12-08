package SupportiveThreads;

import Classes.GameInfo;
import interfaces.AppServerInterface;
import interfaces.DatabaseInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameInfoListReceiver extends Thread {
    DatabaseInterface dataImpl;
    AppServerInterface appImpl;
    ArrayList<GameInfo> gameInfos;
    Set<Integer> gameInfoIds= new HashSet<>();
    public GameInfoListReceiver(AppServerInterface appImpl, DatabaseInterface dataImpl, ArrayList<GameInfo> gameInfoList){
        this.dataImpl= dataImpl;
        this.gameInfos=gameInfoList;
        this.appImpl= appImpl;

        for (GameInfo gameInfo : gameInfos) {
            gameInfoIds.add(gameInfo.getGameId());
        }
    }


    @Override
    public void run() {
        super.run();

        while(true){
            try {


                ArrayList<GameInfo> newList=new ArrayList<>(dataImpl.getGameInfoList(true));
                gameInfos.clear();
                gameInfos.addAll(newList);

                appImpl.notifyGameInfoList();

                System.out.println("nieuwe list ontvangen van database");
            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }

    }
}
