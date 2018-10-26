package client.Game;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//  gewoon een vertaling van
// https://www.youtube.com/watch?time_continue=50&v=QjuytZhQYo8


public class MemoryPuzzleApp extends Application {

    private static final int AANTALPAREN = 8;
    private static final int AANTALPERRIJ = 4;

    public static Tegel selectedTile = null;

    public static int clickCount = 2;  // om te limiteren dat je alles 1000 keer klikt en ales door elkaar loopt en crasht
                                        // is eigenlijk een soort semafoor, wordt wel maar aangesproken in tegel klasse

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(createContent()));
        primaryStage.show();
    }

    private Parent createContent() {
        //setup methode

        Pane root = new Pane();
        root.setPrefSize(600,600); //vierkante puzzel

        //generatie van de tegels in de arraylist
        char c = 'A';
        List<Tegel> tegels = new ArrayList<Tegel>();

        for (int i = 0; i < AANTALPAREN; i++) {
            //tegel met juist getal
            tegels.add(new Tegel(String.valueOf(c))); //2 x zelfde tegel genereren
            tegels.add(new Tegel(String.valueOf(c)));

            c++; //1 karakter opschuiven

        }

        //shuffelen van de tegels in de lijst
        Collections.shuffle(tegels);

        //tegels displayen in de root pane
        for (int i = 0; i < tegels.size(); i++) {

            Tegel tegel = tegels.get(i);

            //x en y setten
            tegel.setTranslateX(50 * (i % AANTALPERRIJ));
            tegel.setTranslateY(50 * (i / AANTALPERRIJ));

            //toevoegen aan de view
            root.getChildren().add(tegel);


        }

        return root;
    }

    public static void main(String[] args){
        launch(args);
    }

}
