package client;

import Classes.Game;

public class CurrentGame {
    private static Game game;

    public static Game getInstance(){
        return game;
    }

    public static void setInstance(Game newGame){
        game=newGame;
    }

    public static void resetGame(){
        game=null;
    }


}
