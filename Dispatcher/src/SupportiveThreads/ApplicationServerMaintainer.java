package SupportiveThreads;

public class ApplicationServerMaintainer extends Thread{

    private final int AANTALGAMESPERAPPSERVER = 3;

    private int aantalBezigOpAppServers;
    private int vorigAantalGames;


    public ApplicationServerMaintainer(){
        aantalBezigOpAppServers = 0;
        vorigAantalGames = aantalBezigOpAppServers;
    }

    @Override
    public void run() {
        super.run();


    }

    /**
     *
     * @param aantalGamesBezig nieuw aantal games
     * @return  1 indien aantal games gestegen is
     *          -1 indien aantal games gedaald is
     */
    public int setAantalGames(int aantalGamesBezig, int aantalActiveAppservers) {
        int returnValue = 0;
        this.aantalBezigOpAppServers = aantalGamesBezig;

        if(aantalBezigOpAppServers > vorigAantalGames && (aantalBezigOpAppServers % AANTALGAMESPERAPPSERVER) == 0){
            System.out.println("gestegen, nieuwe appserver moet aangemaakt worden");
            if(aantalActiveAppservers*3==aantalGamesBezig) returnValue = 1;
        }

        else if(aantalBezigOpAppServers < vorigAantalGames && (aantalBezigOpAppServers % AANTALGAMESPERAPPSERVER) == 0 && aantalBezigOpAppServers != 0){
            System.out.println("gedaald, appserver moet verwijderd worden");

            returnValue = -1;
        }

        vorigAantalGames = aantalGamesBezig;
        return returnValue;
    }
}
