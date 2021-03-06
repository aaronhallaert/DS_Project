package client;

//klasse die de Game klasse in de client voorstelt
//moet hier met andere attributen dan in de application server
//we kunnen geen SimpleStringAttr, ... versturen, daarom in de appserver -> gewone datatypes
//hier wel met speciale datatypes werken (voor de voorstelling)

import Classes.GameInfo;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * stelt lokaal een game voor in de datatable in het lobbyScreen.
 * de params simpleStringProperty enzo, hebben temaken met de voorstelling in de table van de lobby
 *
 * om de 5 seconden vraagt de Client aan de Appserver de lijst van GameInfo's
 * deze lijst met GameInfo elementen wordt dan in een lijst van GameObs gegoten zodat het eenvoudig te visualiseren is
 * in een datatable
 *
 */
public class GameInfoObs {

    private SimpleIntegerProperty maxAantalSpelers;
    private SimpleIntegerProperty gameId;
    private List<SimpleStringProperty> spelers;
    private SimpleIntegerProperty aantalSpelerConnected;
    private SimpleStringProperty fotoSet;
    private SimpleIntegerProperty roosterSize;

    public GameInfoObs(GameInfo gameInfo){
        maxAantalSpelers = new SimpleIntegerProperty(gameInfo.getAantalSpelers());
        gameId = new SimpleIntegerProperty(gameInfo.getGameId());
        spelers= new ArrayList<>();
        for (String speler : gameInfo.getSpelers()) {
            spelers.add(new SimpleStringProperty(speler));
        }
        aantalSpelerConnected = new SimpleIntegerProperty(gameInfo.getAantalSpelersConnected());
        fotoSet = new SimpleStringProperty(gameInfo.getFotoSet());
        roosterSize = new SimpleIntegerProperty(gameInfo.getRoosterSize());
    }

    public String toString(){
        return gameId.getValue() + ", met host: "+ spelers.get(0).getValue()+"...";
    }

    //getters en setters enzo
    public int getGameId() {
        return gameId.get();
    }

    public SimpleIntegerProperty gameIdProperty() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId.set(gameId);
    }

    public int getAantalSpelerConnected() {
        return aantalSpelerConnected.get();
    }

    public SimpleIntegerProperty aantalSpelerConnectedProperty() {
        return aantalSpelerConnected;
    }

    public void setAantalSpelerConnected(int aantalSpelerConnected) { this.aantalSpelerConnected.set(aantalSpelerConnected); }

    public String getFotoSet() {
        return fotoSet.get();
    }

    public SimpleStringProperty fotoSetProperty() {
        return fotoSet;
    }

    public void setFotoSet(String fotoSet) {
        this.fotoSet.set(fotoSet);
    }

    public int getRoosterSize() {
        return roosterSize.get();
    }

    public SimpleIntegerProperty roosterSizeProperty() {
        return roosterSize;
    }

    public void setRoosterSize(int roosterSize) {
        this.roosterSize.set(roosterSize);
    }

    public String getSpelers() {
        StringBuilder sb =new StringBuilder();

        for (SimpleStringProperty speler : spelers) {
            sb.append(speler.get());
            sb.append(", ");
        }

        sb.deleteCharAt(sb.length()-1);
        sb.deleteCharAt(sb.length()-1);

        return sb.toString();
    }

    public int getMaxAantalSpelers() {
        return maxAantalSpelers.get();
    }

    public SimpleIntegerProperty maxAantalSpelersProperty() {
        return maxAantalSpelers;
    }

}
