package client.SupportiveThreads;

import Classes.GameInfo;
import Classes.GameState;
import client.Controllers.SpelViewLogica;
import client.CurrentGame;
import client.Main;
import client.CurrentUser;

import java.rmi.RemoteException;

public class WaitOnTurn extends Thread{
    private SpelViewLogica svl;
    private GameState gameState;
    private GameInfo gameInfo;


    public WaitOnTurn(SpelViewLogica svl, GameInfo gameInfo, GameState gameState){
        this.svl=svl;
        this.gameState=gameState;
        this.gameInfo=gameInfo;
    }

    @Override
    public void run() {
        super.run();


        while(true){


            try {
                if(Main.cnts.getAppImpl().changeInTurn(CurrentGame.getInstance().getGameId(), gameState.getAandeBeurt())){
                    String updatedTurn= Main.cnts.getAppImpl().getGameState(CurrentGame.getInstance().getGameId()).getAandeBeurt();
                    gameState.setAandeBeurt(updatedTurn);
                    if(updatedTurn.equals(CurrentUser.getInstance().getUsername())){
                        System.out.println("ja tis aan mijn beurt");
                        svl.myTurn(true);
                    }
                    else{
                        System.out.println("nee niet aan mijn beurt");
                        svl.myTurn(false);
                    }

                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
