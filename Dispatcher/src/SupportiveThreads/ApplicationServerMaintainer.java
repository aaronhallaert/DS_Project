package SupportiveThreads;

import sun.plugin.com.DispatchImpl;

public class ApplicationServerMaintainer extends Thread{

    private final int AANTALGAMESPERAPPSERVER = 3;

    private int aantalBezigOpAppServer;
    private int vorigAantalGames;


    public ApplicationServerMaintainer(){
        aantalBezigOpAppServer = 0;
        vorigAantalGames = aantalBezigOpAppServer;
    }

    @Override
    public void run() {
        super.run();


    }

    public int setAantalGames(int aantalGamesBezig) {

        int returnValue = 0;
        this.aantalBezigOpAppServer = aantalGamesBezig;

        if(aantalBezigOpAppServer > vorigAantalGames && aantalBezigOpAppServer % AANTALGAMESPERAPPSERVER == 0){

            System.out.println("gestegen, nieuwe appserver moet aangemaakt worden");
            returnValue = 1; // 1 omdat je een value moet ophogen


        }

        else if(aantalBezigOpAppServer < vorigAantalGames && aantalBezigOpAppServer % AANTALGAMESPERAPPSERVER == 0){

            System.out.println("gedaald, appserver moet verwijderd worden");
            returnValue = -1; // -1 omdat je een server moet wegdoen

        }

        vorigAantalGames = aantalGamesBezig;
        return returnValue;


    }
}
