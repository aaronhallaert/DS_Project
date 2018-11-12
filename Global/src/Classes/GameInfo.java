package Classes;

import java.io.Serializable;

public class GameInfo implements Serializable {

    private int gameId;
    private String clientA;
    private String clientB;
    private Integer aantalSpelersConnected;
    private String fotoSet;
    private Integer roosterSize;

    public GameInfo(){
        this.gameId = 0;
        this.clientA = "";
        this.clientB = "";
        this.aantalSpelersConnected = 0;
        this.roosterSize = 0;
        this.fotoSet = "";
    }

    /** opgeroepen bij het maken van een nieuwe game!
     *  wordt opgeroepen vanuit de clientB
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

    public synchronized boolean join(String user){
        System.out.println("client "+ user+ " probeert te joinen");
        if(!clientA.equals(user) && clientB.equals("")){
            clientB = user;
            aantalSpelersConnected++;
            System.out.println("notifyke join");
            notifyAll();
            System.out.println("join in gameInfo succesvol");
            return true;
        }
        else{
            return false;
        }


    }


    public synchronized boolean changeInPlayers(int currentNumberOfPlayers){
        System.out.println("jawel hoor");
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
