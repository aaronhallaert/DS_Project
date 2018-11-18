package Classes;

import java.io.Serializable;

/**
 * is altijd een deel van een Game
 * voornamelijk de zaken om de game voor te stellen in de lobbyTabel
 */
public class GameInfo implements Serializable {


    private int gameId; //random gegenereerd nummer tussen 1 en 1000
    private String clientA; // de speler die de game creert
    private String clientB; // de speler die eeen gecreerede game joint
    private int aantalSpelersConnected;
    private String fotoSet; // variabele die aangeeft welke preset van fotos er zal ingeladen worden
    private int roosterSize; // grootte van het rooster

    /** wordt nergens opgeroepen, tenzij er ergens iets fout gaat (een game die niet gevonden wordt bvb)
     *      als de game niet gevonden wordt wordt een lege nieuwe game opgeroepen
     */
    public GameInfo(){
        this.gameId = 0;
        this.clientA = "";
        this.clientB = "";
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
    public GameInfo(int gameId, String hostName, int dimensions, char set){
        System.out.println("game constructor in appserver, Game.java triggerd");
        this.gameId = gameId;
        this.clientA = hostName;
        this.clientB = "";
        this.aantalSpelersConnected =1;
        this.roosterSize = dimensions;
        this.fotoSet = set+"";

    }

    public GameInfo(int gameId, String clientA, String clientB, int aantalSpelersConnected, String fotoSet, int roosterSize) {
        this.gameId=gameId;
        this.clientB=clientB;
        this.clientA=clientA;
        this.aantalSpelersConnected=aantalSpelersConnected;
        this.fotoSet=fotoSet;
        this.roosterSize=roosterSize;
    }


    public String toString(){
        return"game met clientA: "+ clientA +", clientB: "+ clientB +" fotoset: "+fotoSet+" sizerooster = "+roosterSize+"X"+roosterSize;
    }


    /**
     * join game
     * @param user naam van user die probeert te joinen
     * @return true on success, false on fail
     */
    public synchronized boolean join(String user){
        System.out.println("client "+ user+ " probeert te joinen");

        // als het een initiele join is
        if(!clientA.equals(user) && clientB.equals("")){
            clientB = user;
            aantalSpelersConnected++;
            notifyAll();
            return true;
        }

        //todo: check rejoin
        else if(clientA.equals(user) || clientB.equals(user)){
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
        if(userName.equals(clientA) || userName.equals(clientB)) {
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

    public String getClientA() {
        return clientA;
    }

    public void setClientA(String clientA) {
        this.clientA = clientA;
    }

    public String getClientB() {
        return clientB;
    }

    public void setClientB(String clientB) {
        this.clientB = clientB;
    }

    public Integer getAantalSpelersConnected() {
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

    public Integer getRoosterSize() {
        return roosterSize;
    }

    public void setRoosterSize(Integer roosterSize) {
        this.roosterSize = roosterSize;
    }


}
