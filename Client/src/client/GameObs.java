package client;

//klasse die de Game klasse in de client voorstelt
//moet hier met andere attributen dan in de application server
//we kunnen geen SimpleStringAttr, ... versturen, daarom in de appserver -> gewone datatypes
//hier wel met speciale datatypes werken (voor de voorstelling)

import Classes.GameInfo;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * stelt lokaal een game voor
 * de params simpleStringProperty enzo, hebben temaken met de voorstelling in de table van de lobby
 *
 */
public class GameObs  {

    private SimpleIntegerProperty gameId;
    private SimpleStringProperty host;
    private SimpleStringProperty client;
    private SimpleIntegerProperty aantalSpelerConnected;
    private SimpleStringProperty fotoSet;
    private SimpleIntegerProperty roosterSize;

    public GameObs(GameInfo gameInfo){
        gameId = new SimpleIntegerProperty(gameInfo.getGameId());
        host = new SimpleStringProperty(gameInfo.getClientA());
        client = new SimpleStringProperty(gameInfo.getClientB());
        aantalSpelerConnected = new SimpleIntegerProperty(gameInfo.getAantalSpelersConnected());
        fotoSet = new SimpleStringProperty(gameInfo.getFotoSet());
        roosterSize = new SimpleIntegerProperty(gameInfo.getRoosterSize());
    }

    public String toString(){
        return gameId.getValue() + ", met host: "+host.getValue()+"...";
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

    public String getHost() {
        return host.get();
    }

    public SimpleStringProperty hostProperty() {
        return host;
    }

    public void setHost(String host) {
        this.host.set(host);
    }

    public String getClient() {
        return client.get();
    }

    public SimpleStringProperty clientProperty() {
        return client;
    }

    public void setClient(String client) {
        this.client.set(client);
    }

    public int getAantalSpelerConnected() {
        return aantalSpelerConnected.get();
    }

    public SimpleIntegerProperty aantalSpelerConnectedProperty() {
        return aantalSpelerConnected;
    }

    public void setAantalSpelerConnected(int aantalSpelerConnected) {
        this.aantalSpelerConnected.set(aantalSpelerConnected);
    }

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
