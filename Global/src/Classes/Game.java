package Classes;

import java.io.Serializable;

/**
 * https://code.makery.ch/library/javafx-tutorial/part2/
 * datatype die zal proberen een actieve game voor te stellen
 *
 * property datatypes, zullen helpen als iets veranderd is het te notifyen
 *
 * todo? game class zou eigenlijk ook nog moeten apart staan in de client
 *
 * BEVAT DE INFO OVER DE GAME : CONNECTIES? USERS? FINISHED? ALLE MOG OM TE JOINEN
 * DE EFFECTIEVE REPRESENTATIE VAN DE GAME ZIT EM IN GAMESTATE
 *
 */
public class Game implements Serializable {

    private int gameId;
    private GameInfo gameInfo; //bevat alle data om connecties te maken tot een spelletje, metadata als het ware
    private GameState gameState; // bevat alles dat een game voorstelt, we kunnen een staat inladen en dan verdergaan
                                 // met een spelletje

    //tot nu toe bevat gameState en gameInfo wat redundant information

    public Game(){ // zal nooit worden opgeroepen!
        gameId = 0;
        gameInfo = new GameInfo();
        gameState = new GameState();
    }

    /** opgeroepen bij het maken van een nieuwe game!
     *  wordt opgeroepen vanuit de client
     * @param hostName -> parameter voor de gameInfo
     * @param dimensions ->param voor gameInfo en gameState
     * @param set -> voor de gameState?
     */
    public Game(int gameId, String hostName, int dimensions, char set){

        System.out.println("game wordt gemaakt...");

        this.gameId = gameId;

        gameInfo = new GameInfo(gameId, hostName, dimensions, set);
        System.out.println("gameInfo made");

        gameState = new GameState(gameId, dimensions,set);
        System.out.println("gameState made");
    }

    public int getGameId(){return gameId;}

    public GameInfo getGameInfo(){
        return gameInfo;
    }

    public GameState getGameState() { return gameState; }

    public String toString(){
        return gameInfo.toString() + "\n" + gameState.toString();
    }

}
