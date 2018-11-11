package Classes;

import javafx.scene.layout.StackPane;

import java.io.Serializable;

public class Tile implements Serializable {

    //tile = de waarden van de tegel -> niet de voorsetlling, does not extends stackpane dus
    //tegelVisueel = de visuele voorstelling van de tegel.

    private int uniqueIdentifier;
    private int id;
    private String imageId;
    private String backImageId;

    //extras?
    private boolean found;
    private boolean flippedOver;

    //bevat de afbeelding niet!, bevat wel de naam van de afbeelding mss best
    public Tile(int uniqueIdentifier, int id, String imageId, String backImageId){
        this.uniqueIdentifier = uniqueIdentifier;
        this.id = id;
        this.imageId = imageId;
        this.backImageId = backImageId;

        //extras?
        found = false;
        flippedOver= false;
    }

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
