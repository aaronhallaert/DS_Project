package Classes;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * is altijd een deel van een Game
 * voornamelijk de zaken om de game voor te stellen in de lobbyTabel
 */
public class GameInfo implements Serializable {


    private int gameId; //random gegenereerd nummer tussen 1 en 1000
    private ArrayList<String> spelers;
    private int aantalSpelers;
    private int aantalSpelersConnected;
    private String fotoSet; // variabele die aangeeft welke preset van fotos er zal ingeladen worden
    private int roosterSize; // grootte van het rooster
    private int appServerPoort;

    /** wordt nergens opgeroepen, tenzij er ergens iets fout gaat (een game die niet gevonden wordt bvb)
     *      als de game niet gevonden wordt wordt een lege nieuwe game opgeroepen
     */
    public GameInfo(){
        this.gameId = 0;
        this.spelers= new ArrayList<>();
        this.aantalSpelersConnected = 0;
        this.roosterSize = 0;
        this.fotoSet = "";
    }

    /** opgeroepen bij het maken van een nieuwe game
     *  de speler die de game creeerde is speler A
     * @param gameId
     * @param hostName
     * @param dimensions
     * @param set
     */
    public GameInfo(int gameId, String hostName, int dimensions, char set, int aantalSpelers, int appServerPoort){
        System.out.println("game constructor in appserver, Game.java triggerd");
        this.gameId = gameId;
        this.spelers= new ArrayList<>();
        spelers.add(hostName);
        this.aantalSpelersConnected =1;
        this.roosterSize = dimensions;
        this.fotoSet = set+"";

        this.aantalSpelers=aantalSpelers;
        this.appServerPoort=appServerPoort;

    }

   /* public GameInfo(int gameId, ArrayList<String> spelers, int aantalSpelersConnected, String fotoSet, int roosterSize) {
        System.out.println("spciale GameInfo constructor opgeroepen die we normaal niet meer gebruiken!");
        this.gameId=gameId;
        this.spelers=spelers;
        this.aantalSpelersConnected=aantalSpelersConnected;
        this.fotoSet=fotoSet;
        this.roosterSize=roosterSize;

        // TODO keuze aantal spelers
        this.aantalSpelers=3;
    }*/


    public String toString(){
        StringBuilder sb= new StringBuilder();

        sb.append("game met clients: \n");
        for (String speler : spelers) {
            sb.append(speler+ "\n");
        }

        sb.append("met  fotoset: "+fotoSet+" sizerooster = "+roosterSize+"X"+roosterSize);


        return sb.toString();
    }


    /**
     * join game
     * @param user naam van user die probeert te joinen
     * @return true on success, false on fail
     */
    public synchronized boolean join(String user){
        System.out.println("client "+ user+ " probeert te joinen");

        if(spelers.size() < aantalSpelers && !spelers.contains(user)){
            // initiele join van deze speler
            spelers.add(user);
            aantalSpelersConnected++;
            notifyAll();
            return true;
        }
        else if(spelers.contains(user)){
            // rejoin
            aantalSpelersConnected++;
            notifyAll();
            return true;
        }
        else{
            return false;
        }



    }

    public synchronized boolean changeInPlayers(int aantalSpelers) {
        while(aantalSpelers==aantalSpelersConnected){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return true;

    }

    public synchronized void playerLeaves(String userName) {

        //alleen als de 'leaver' een speler is
        if(spelers.contains(userName)){
            aantalSpelersConnected--;
        }
        notifyAll();
    }

    /* GETTERS SETTERS */
    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }


    public int getAantalSpelersConnected() {
        return aantalSpelersConnected;
    }

    public void setAantalSpelersConnected(int aantalSpelersConnected) {
        this.aantalSpelersConnected = aantalSpelersConnected;
    }

    public String getFotoSet() {
        return fotoSet;
    }

    public void setFotoSet(String fotoSet) {
        this.fotoSet = fotoSet;
    }

    public int getRoosterSize() {
        return roosterSize;
    }

    public ArrayList<String> getSpelers() {
        return spelers;
    }

    public void setSpelers(ArrayList<String> spelers) {
        this.spelers = spelers;
    }

    public int getAantalSpelers() {
        return aantalSpelers;
    }

    public void setAantalSpelers(int aantalSpelers) {
        this.aantalSpelers = aantalSpelers;
    }

    public void setRoosterSize(int roosterSize) {
        this.roosterSize = roosterSize;
    }

    public int getAppServerPoort() {
        return appServerPoort;
    }

    public void setAppServerPoort(int appServerPoort) {
        this.appServerPoort = appServerPoort;
    }
}
