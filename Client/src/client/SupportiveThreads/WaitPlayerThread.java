package client.SupportiveThreads;

import Classes.GameInfo;
import client.Controllers.SpelViewLogica;
import client.CurrentGame;
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
                System.out.println("voorlopig heb ik "+ gameInfo.getAantalSpelersConnected()+ " geconnecteerde spelers, is er verandering?");
                if(Main.cnts.getAppImpl().changeInPlayers(CurrentGame.getInstance().getGameId(), gameInfo.getAantalSpelersConnected())) {
                    int updateValuePlayers= Main.cnts.getAppImpl().getGameInfo(CurrentGame.getInstance().getGameId()).getAantalSpelersConnected();
                    if(updateValuePlayers==2){
                        svl.bothPlayersConnected(true);
                        gameInfo.setAantalSpelersConnected(2);
                        System.out.println("beide spelers zijn geconnecteerd");
                    }
                    else{
                        svl.bothPlayersConnected(false);
                        gameInfo.setAantalSpelersConnected(updateValuePlayers);
                        System.out.println("niet alle spelers zijn geconnecteerd");
                    }



                }

            }
            catch(Exception e){
                e.printStackTrace();
            }

        }


    }
}
