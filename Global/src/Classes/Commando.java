package Classes;

import java.io.Serializable;

/**
 * een commando wordt verstuurd van de applicationserver naar een of beide clients in game
 * afhankelijk van het type van het commando, gebeuren er verschillende zaken lokaal in de client
 */
public class Commando implements Serializable {

    // 1 indien niet van toepassing op het commando (vb WIN, LOSS) : deze commandos werken niet in op een tile
    private int uniqueTileId;

    private String effectOnUser;
    /*
      FLIP, UNFLIP van een kaartje,
      SWITCH (user switch),
      LOCK, UNLOCK (tile clickable en unclickable maken)
      AWARDTOME, AWARDTOYOU : jezelf/ andere speler een punt geven
      WIN, LOSS, DRAW : einde van spel visualiseren
    */
    private String type;


    public Commando(String type, int tileId){

        this.type = type;
        this.uniqueTileId = tileId;

    }

    public Commando(String type, String activeUser, int i) {
        this.type= type;
        this.uniqueTileId= i;
        this.effectOnUser= activeUser;
    }


    public String getType() {
        return type;
    }

    public String getEffectOnUser() {
        return effectOnUser;
    }

    public void setEffectOnUser(String effectOnUser) {
        this.effectOnUser = effectOnUser;
    }

    public int getUniqueTileId() {
        return uniqueTileId;
    }
}
