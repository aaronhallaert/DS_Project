package client.SupportiveThreads;

import Classes.GameInfo;
import client.Controllers.SpelViewLogica;
import client.Main;

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



        while(true) {

            try {

                if(Main.cnts.getAppImpl().changeInPlayers(gameInfo.getAantalSpelersConnected(), Main.currentGameId)) {
                    svl.changeInNumberOfPlayers();
                    gameInfo.setAantalSpelersConnected(Main.cnts.getAppImpl().getGameInfo(Main.currentGameId).getAantalSpelersConnected());
                    System.out.println("aantal players is verandert, mouseclick wordt al dan niet gedisabled");
                }

            }
            catch(Exception e){
                e.printStackTrace();
            }

        }


    }
}
