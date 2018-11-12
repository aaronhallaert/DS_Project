package Classes;

import java.io.Serializable;

public class Commando implements Serializable {

    private String commandoType;// -> state maken, :FLIP , UNFLIP
    private int uniqueTileId; // visualtile.id

    public Commando(String commandoType, int tileId){
        this.commandoType =commandoType;
        this.uniqueTileId = tileId;
    }

    public String getCommandoType() {
        return commandoType;
    }

    public int getUniqueTileId() {
        return uniqueTileId;
    }
}
