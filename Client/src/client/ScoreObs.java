package client;

import Classes.Score;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;

public class ScoreObs {

    private SimpleStringProperty  naamSpeler;
    private SimpleIntegerProperty aantalGames;
    private SimpleIntegerProperty aantalWins;
    private SimpleIntegerProperty aantalDraws;
    private SimpleIntegerProperty aantalLosses;
    private SimpleIntegerProperty aantal4X4;
    private SimpleIntegerProperty aantal6X6;

    public ScoreObs(Score score) {
        this.naamSpeler = new SimpleStringProperty(score.getNaam());
        ArrayList<Integer> values = score.getValues();


        aantalWins = new SimpleIntegerProperty(values.get(0));
        aantalDraws = new SimpleIntegerProperty(values.get(1));
        aantalLosses = new SimpleIntegerProperty(values.get(2));
        aantal4X4 = new SimpleIntegerProperty(values.get(3));
        aantal6X6 = new SimpleIntegerProperty(values.get(4));
        aantalGames =new SimpleIntegerProperty(values.get(5));
    }


    public String getNaamSpeler() {
        return naamSpeler.get();
    }

    public SimpleStringProperty naamSpelerProperty() {
        return naamSpeler;
    }

    public void setNaamSpeler(String naamSpeler) {
        this.naamSpeler.set(naamSpeler);
    }

    public int getAantalGames() {
        return aantalGames.get();
    }

    public SimpleIntegerProperty aantalGamesProperty() {
        return aantalGames;
    }

    public void setAantalGames(int aantalGames) {
        this.aantalGames.set(aantalGames);
    }

    public int getAantalWins() {
        return aantalWins.get();
    }

    public SimpleIntegerProperty aantalWinsProperty() {
        return aantalWins;
    }

    public void setAantalWins(int aantalWins) {
        this.aantalWins.set(aantalWins);
    }

    public int getAantalDraws() {
        return aantalDraws.get();
    }

    public SimpleIntegerProperty aantalDrawsProperty() {
        return aantalDraws;
    }

    public void setAantalDraws(int aantalDraws) {
        this.aantalDraws.set(aantalDraws);
    }

    public int getAantalLosses() {
        return aantalLosses.get();
    }

    public SimpleIntegerProperty aantalLossesProperty() {
        return aantalLosses;
    }

    public void setAantalLosses(int aantalLosses) {
        this.aantalLosses.set(aantalLosses);
    }

    public int getAantal4X4() {
        return aantal4X4.get();
    }

    public SimpleIntegerProperty aantal4X4Property() {
        return aantal4X4;
    }

    public void setAantal4X4(int aantal4X4) {
        this.aantal4X4.set(aantal4X4);
    }

    public int getAantal6X6() {
        return aantal6X6.get();
    }

    public SimpleIntegerProperty aantal6X6Property() {
        return aantal6X6;
    }

    public void setAantal6X6(int aantal6X6) {
        this.aantal6X6.set(aantal6X6);
    }

}
