package Classes;

import javafx.scene.layout.StackPane;

import java.io.Serializable;

/**
 * deze klasse dient om een tile voor te stellen in de APPSERVER, dus niet in de CLIENT
 * deze klasse bevat al het nodige om een VisualTile object in te laden
 * de voornamelijke verschillen zitten hem in :
 *          - VisualTile bevat animaties om om te draaien
 *          - VisualTile bevat de byte[] voorkantFoto en achterkantFoto, terwijl Tile enkel de URL of naam bevat om de byte[] te
 *              krijgen uit de database
 *
 *
 * Dus :    Tile = bevat al het nodige in de appserver om een echte VisualTile te maken in de client
 *
 */
public class Tile implements Serializable {

    private int uniqueIdentifier; // elke tile van een spelletje heeft een verschillende uniqueIdentifier
    private int id;               // in 1 spelletje zijn er telkens 2 tiles met dezelfde id -> gebruikt om te vergelijken als dezelfde
    private String imageId;       // onder welke naam de voorkant afbeelding in de databank zit
    private String backImageId;   // idem maar voor achterkant

    //extras
    private boolean found;        // eenmaal gevonden -> true, anders -> false
    private boolean flippedOver;  // als het kaartje eens gedraaid is


    public Tile(int uniqueIdentifier, int id, String imageId, String backImageId){
        this.uniqueIdentifier = uniqueIdentifier;
        this.id = id;
        this.imageId = imageId;
        this.backImageId = backImageId;

        //extras
        found = false;
        flippedOver= false;
    }

    public Tile(int uniqueIdentifier, int id, String imageId, String backImageId, boolean found, boolean flippedOver) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.id = id;
        this.imageId = imageId;
        this.backImageId = backImageId;

        //extras
        this.found = found;
        this.flippedOver= flippedOver;
    }

    //getters en setters
    public int getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(int uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImageId() {
        return imageId;
    }

    public String getBackImageId() {
        return backImageId;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public boolean isFlippedOver() {
        return flippedOver;
    }

    public void setFlippedOver(boolean flippedOver) {
        this.flippedOver = flippedOver;
    }
}
