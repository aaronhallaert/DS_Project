package client;

//klasse die de Game klasse in de client voorstelt
//moet hier met andere attributen dan in de application server
//we kunnen geen SimpleStringAttr, ... versturen, daarom in de appserver -> gewone datatypes
//hier wel met speciale datatypes werken (voor de voorstelling)

import Classes.GameInfo;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * stelt lokaal een game voor in de datatable in het lobbyScreen.
 * de params simpleStringProperty enzo, hebben temaken met de voorstelling in de table van de lobby
 *
 * om de 5 seconden vraagt de Client aan de Appserver de lijst van GameInfo's
 * deze lijst met GameInfo elementen wordt dan in een lijst van GameObs gegoten zodat het eenvoudig te visualiseren is
 * in een datatable
 *
 */
public class GameObs  {

    private SimpleIntegerProperty gameId;
    private SimpleStringProperty clientA;
    private SimpleStringProperty clientB;
    private SimpleIntegerProperty aantalSpelerConnected;
    private SimpleStringProperty fotoSet;
    private SimpleIntegerProperty roosterSize;

    public GameObs(GameInfo gameInfo){
        gameId = new SimpleIntegerProperty(gameInfo.getGameId());
        clientA = new SimpleStringProperty(gameInfo.getClientA());
        clientB = new SimpleStringProperty(gameInfo.getClientB());
        aantalSpelerConnected = new SimpleIntegerProperty(gameInfo.getAantalSpelersConnected());
        fotoSet = new SimpleStringProperty(gameInfo.getFotoSet());
        roosterSize = new SimpleIntegerProperty(gameInfo.getRoosterSize());
    }

    public String toString(){
        return gameId.getValue() + ", met clientA: "+ clientA.getValue()+"...";
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

    public String getClientA() {
        return clientA.get();
    }

    public SimpleStringProperty clientAProperty() {
        return clientA;
    }

    public void setClientA(String clientA) {
        this.clientA.set(clientA);
    }

    public String getClientB() {
        return clientB.get();
    }

    public SimpleStringProperty clientBProperty() {
        return clientB;
    }

    public void setClientB(String client) {
        this.clientB.set(client);
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


}
