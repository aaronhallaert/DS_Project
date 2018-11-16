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
    private Integer aantalSpelersConnected;
    private String fotoSet; // variabele die aangeeft welke preset van fotos er zal ingeladen worden
    private Integer roosterSize; // grootte van het rooster

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


    public String toString(){
        return"game met clientA: "+ clientA +", clientB: "+ clientB +" fotoset: "+fotoSet+" sizerooster = "+roosterSize+"X"+roosterSize;
    }


    // wordt door speler2 opgeroepen,
    // todo: zorgen dat speler 1 ook kan joinen
    public synchronized boolean join(String user){
        System.out.println("client "+ user+ " probeert te joinen");

        // als het een initiele join is
        if(!clientA.equals(user) && clientB.equals("")){
            clientB = user;
            System.out.println("joiner is set in Application server");
            aantalSpelersConnected++;
            System.out.println("notifyke join");
            notifyAll();
            System.out.println("join in gameInfo succesvol");
            return true;
        }

        //todo: wat als er geen initiele join is?
        else{
            /*
             kijken welke user het is, als de user niet klopt geef je een error
             als speler 1,
             als speler 2,..
             spelerConnected++

             */
            return false;
        }

    }


    /** wordt getriggerd als speler 1 in het spel zit, en speler 2 terug joint
     *
     * @param currentNumberOfPlayers hoeveel spelers er gejoind zijn volgens de client
     * @return
     */
    public synchronized boolean changeInPlayers(int currentNumberOfPlayers){

        while(currentNumberOfPlayers==aantalSpelersConnected){
            try {
                System.out.println("CIP wait begin");
                wait();
                System.out.println("CIP wait done");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return true;
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

    public void setAantalSpelersConnected(Integer aantalSpelersConnected) {
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
