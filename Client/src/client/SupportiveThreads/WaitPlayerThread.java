package client.SupportiveThreads;

import Classes.GameInfo;
import client.Controllers.SpelViewLogica;
import client.CurrentGame;
import client.Main;

import java.rmi.RemoteException;

/**
 * deze thread wacht op de 2e speler (opdat hij gejoind is) alvorens de eerste speler kan beginnen met zijn kaartjes
 * om te draaien
 */
public class WaitPlayerThread extends Thread {


    SpelViewLogica svl;
    GameInfo gameInfo;

    public WaitPlayerThread(SpelViewLogica svl, GameInfo gameInfo){
        this.svl=svl;
        this.gameInfo=gameInfo;
    }

    @Override
    public void run() {
        super.run();

        try {

        while(true) {


               // System.out.println("voorlopig heb ik "+ gameInfo.getAantalSpelersConnected()+ " geconnecteerde spelers, is er verandering?");
                if(Main.cnts.getAppImpl().changeInPlayers(CurrentGame.getInstance().getGameId(), gameInfo.getAantalSpelersConnected())) {

                    int updateValuePlayers= Main.cnts.getAppImpl().getGameInfo(CurrentGame.getInstance().getGameId()).getAantalSpelersConnected();

                    if (updateValuePlayers == CurrentGame.getInstance().getGameState().getAantalSpelers()){
                        svl.bothPlayersConnected(true);
                        gameInfo.setAantalSpelersConnected(updateValuePlayers);
                        System.out.println("alle spelers zijn geconnecteerd");


                    }
                    else{
                        svl.bothPlayersConnected(false);
                        gameInfo.setAantalSpelersConnected(updateValuePlayers);
                       // System.out.println("nog niet alle spelers zijn geconnecteerd");
                    }

                    svl.updateGame();
                    svl.updateScore();
                }

            }


        }
        catch(RemoteException e){
            System.out.println("application server is uitgevallen");
        }


    }
}
