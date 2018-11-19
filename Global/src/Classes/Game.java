package Classes;

import java.io.Serializable;

/**
 * stelt actieve game voor
 *
 */
public class Game implements Serializable {


    private int gameId;

    // gameInfo en gameState delen naam van clients

    //bevat alle data om connecties te maken tot een spelletje, 'metadata'
    private GameInfo gameInfo;


    // bevat alles dat een game voorstelt (flipped tiles, volgorde tiles...)
    // we kunnen een staat inladen en dan verdergaan met een spelletje
    // hier gebeurt ook de application server logica van het spelletje
    private GameState gameState;


    // wordt nergens opgeroepen, tenzij er ergens iets fout gaat (een game die niet gevonden wordt bvb)
    public Game(){
        gameId = 0;
        gameInfo = new GameInfo();
        gameState = new GameState();
    }

    public Game(int gameId, GameInfo gameInfo, GameState gameState){
        this.gameId= gameId;
        this.gameInfo=gameInfo;
        this.gameState=gameState;
    }

    /**
     * @param hostName
     * @param dimensions grootte van het rooster (6X6 of 4X4)
     * @param set de set van afbeeldingen die we willen gebruiken
     */
    public Game(int gameId, String hostName, int dimensions, char set, int aantalSpelers){

        System.out.println("game wordt gemaakt...");

        this.gameId = gameId;

        gameInfo = new GameInfo(gameId, hostName, dimensions, set, aantalSpelers);
        System.out.println("gameInfo made");

        gameState = new GameState(gameId, dimensions,set, hostName, aantalSpelers);
        System.out.println("gameState made");
    }


    // GETTERS AND SETTERS

    public int getGameId(){return gameId;}

    public GameInfo getGameInfo(){
        return gameInfo;
    }

    public GameState getGameState() { return gameState; }

    public String toString(){
        return gameInfo.toString() + "\n" + gameState.toString();
    }

}
