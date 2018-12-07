package SupportiveThreads;

import Classes.GameInfo;
import interfaces.AppServerInterface;
import interfaces.DatabaseInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GameInfoListReceiver extends Thread {
    DatabaseInterface dataImpl;
    AppServerInterface appImpl;
    Set<GameInfo> gameInfos;
    Set<Integer> gameInfoIds= new HashSet<>();
    public GameInfoListReceiver(AppServerInterface appImpl, DatabaseInterface dataImpl, Set<GameInfo> gameInfoList){
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
                for (GameInfo gameInfo : dataImpl.getGameInfoList(gameInfos.size())) {
                    if(!gameInfoIds.contains(gameInfo.getGameId())){
                        gameInfoIds.add(gameInfo.getGameId());
                        gameInfos.add(gameInfo);
                    }
                }

                appImpl.notifyGameInfoList();

                System.out.println("nieuwe list ontvangen van database");
            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }

    }
}
