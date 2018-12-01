package SupportiveThreads;

import sun.plugin.com.DispatchImpl;

public class ApplicationServerMaintainer extends Thread{

    private final int AANTALGAMESPERAPPSERVER = 3;

    private Integer aantalBezigOpAppServer;
    private int vorigAantalGames;


    public ApplicationServerMaintainer(){
        aantalBezigOpAppServer = 0;
        vorigAantalGames = aantalBezigOpAppServer;
    }

    @Override
    public void run() {
        super.run();


    }

    private void setGelijk() {
    }

    public void setAantalGames(Integer aantalGamesBezig) {
        this.aantalBezigOpAppServer = aantalGamesBezig;

        if(aantalBezigOpAppServer > vorigAantalGames && aantalBezigOpAppServer % AANTALGAMESPERAPPSERVER == 0){

            System.out.println("gestegen, nieuwe appserver moet aangemaakt worden");


        }

        else if(aantalBezigOpAppServer > vorigAantalGames && aantalBezigOpAppServer % AANTALGAMESPERAPPSERVER == 0){

            System.out.println("gedaald, appserver moet verwijderd worden");

        }

        vorigAantalGames = aantalGamesBezig;


    }
}
