package Classes;

import java.io.Serializable;
import java.util.ArrayList;

public class Score implements Serializable {
    private String naam;
    private int aantalWins;
    private int aantalDraws;
    private int aantalLosses;
    private int max4x4;
    private int max6x6;
    private int aantalGames;

    public Score(){
        //configure this
    }

    public Score(String naam, int wins, int draws, int losses, int max4x4, int max6x6, int aantalGames) {
        this.naam = naam;
        this.aantalWins = wins;
        this.aantalDraws = draws;
        this.aantalLosses = losses;
        this.max4x4 = max4x4;
        this.max6x6 = max6x6;
        this.aantalGames = aantalGames;
    }

    public String getNaam(){
        return naam;
    }

    public ArrayList<Integer> getValues(){
        ArrayList<Integer> values = new ArrayList<>();

        values.add(aantalWins);
        values.add(aantalDraws);
        values.add(aantalLosses);
        values.add(max4x4);
        values.add(max6x6);
        values.add(aantalGames);

        return values;
    }
}
