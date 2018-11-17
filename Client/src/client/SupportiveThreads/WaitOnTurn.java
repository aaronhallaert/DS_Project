package client.SupportiveThreads;

import Classes.GameInfo;
import Classes.GameState;
import client.Controllers.SpelViewLogica;
import client.Main;
import client.User;

import java.rmi.RemoteException;

public class WaitOnTurn extends Thread{
    private SpelViewLogica svl;
    private GameState gameState;
    private GameInfo gameInfo;

    private char myCharUser;

    public WaitOnTurn(SpelViewLogica svl, GameInfo gameInfo, GameState gameState){
        this.svl=svl;
        this.gameState=gameState;
        this.gameInfo=gameInfo;
    }

    @Override
    public void run() {
        super.run();

        if(User.getCurrentUser().getUsername().equals(gameInfo.getClientA())){
            myCharUser='A';
        }else{
            myCharUser='B';
        }

        while(true){
            try {
                if(Main.cnts.getAppImpl().changeInTurn(Main.currentGameId, gameState.getAandeBeurt())){
                    char updatedTurn= Main.cnts.getAppImpl().getGameSate(Main.currentGameId).getAandeBeurt();
                    gameState.setAandeBeurt(updatedTurn);
                    if(updatedTurn==myCharUser){
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
