package Classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/*
    hierin komt alles dat de gameState kan bepalen
    NIET DENKEN AAN VISUALISATIE, WEL AAN WAT NODIG IS OM GUI TE VORMEN -> light weight
 */
public class GameState implements Serializable {

    // wie aan de beurt
    // scores
    private int aantalParen;
    private int aantalPerRij;

    //elke tegel moet bijgehouden worden, een tegel kan gewoon meerdere shit bevatten
    private ArrayList<Tile> tegelsList;

    public GameState(){
        tegelsList = new ArrayList<Tile>();
        aantalPerRij = 0;
        aantalParen = 0;
    }

    public GameState(int gameId, int dimensions, char fotoSet) {
        aantalParen = dimensions*dimensions /2;
        aantalPerRij = dimensions;

        int offset = 0;
        if(fotoSet == 'B'){offset = 100;}
        else if(fotoSet == 'C'){offset = 300;}

        tegelsList = new ArrayList<Tile>();

        int uniqueId = 1;

        for(int i=0 ; i<aantalParen ; i++){

            tegelsList.add(new Tile(uniqueId,i, i+offset+1+"", "0" ));
            uniqueId++;
            tegelsList.add(new Tile(uniqueId,i, i+offset+1+"", "0" ));
            uniqueId++;
        }

        Collections.shuffle(tegelsList);



    }

    public int getAantalParen(){return aantalParen;}
    public int getAantalPerRij(){return aantalPerRij;}
    public ArrayList<Tile> getTegelsList(){return tegelsList;}


}
