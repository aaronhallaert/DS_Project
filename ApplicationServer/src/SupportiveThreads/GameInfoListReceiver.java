package SupportiveThreads;

import Classes.GameInfo;
import interfaces.DatabaseInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GameInfoListReceiver extends Thread {
    DatabaseInterface dataImpl;
    Set<GameInfo> gameInfos;
    Set<Integer> gameInfoIds= new HashSet<>();
    public GameInfoListReceiver(DatabaseInterface dataImpl, Set<GameInfo> gameInfoList){
        this.dataImpl= dataImpl;
        this.gameInfos=gameInfoList;

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


                System.out.println("nieuwe list ontvangen van database");
            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }

    }
}
