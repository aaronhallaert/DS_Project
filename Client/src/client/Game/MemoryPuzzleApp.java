package client.Game;

import client.Main;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//  gewoon een vertaling van
// https://www.youtube.com/watch?time_continue=50&v=QjuytZhQYo8


public class MemoryPuzzleApp extends Application {

    public static int AANTALPAREN;
    private static int AANTALPERRIJ; //aantal blokjes op 1 rij in het spellement
    private static int TEGEL_SIZE = 100;
    private static int SPACING_TEGEL=10;
    public static int clickCount = 2;
    public static int AANTALFOUND = 0;
    static Tegel selectedTegel;

    public static int scoreSpelerA = 0;
    public static int scoreSpelerB = 0;
    public static Label finishedLabel;
    public static Label scoreSpelerALabel;
    public static Label scoreSpelerBLabel;
    public static Label aanwieIsHetLabel;
    static State speleradBeurt;

    //TODO: remove this method when done
    //generatie van het rooster - OUD
    private Parent createContent() {

        //achterkant, Diepe kopie van de byte array in elke Tegel?
        //hier maar 1 keer inladen en dan als argument meegeven

        Pane root = new Pane();
        root.setPrefSize(600,600); //vierkante puzzel

        //generatie van 1 tegel
        Tegel tegel = new Tegel(1,Main.loadImageBytes("zezrz"), Main.loadImageBytes("fotoSetC"));

        tegel.setTranslateX(0);
        tegel.setTranslateY(0);

        root.getChildren().add(tegel);

        return root;
    }

    //2e generatie maar dan met parameters
    private Parent createContent(int sizeRooster, char fotoSet) {

        System.out.println("createcontent met params started");
        System.out.println("params: sizeRooster="+sizeRooster+", fotoset="+fotoSet);
        AANTALPAREN = (sizeRooster * sizeRooster) / 2 ;
        AANTALPERRIJ = sizeRooster;

        finishedLabel = new Label("klaar!");
        finishedLabel.setTranslateX((sizeRooster *TEGEL_SIZE + 100)/2);
        finishedLabel.setTranslateY(sizeRooster *TEGEL_SIZE + 100);
        finishedLabel.setVisible(false);

        //Setup van al da tuug
        scoreSpelerALabel = new Label();
        scoreSpelerALabel.setTranslateX((sizeRooster *TEGEL_SIZE + 100));
        scoreSpelerALabel.setTranslateY(sizeRooster *TEGEL_SIZE + 100);
        scoreSpelerALabel.setText("Score Speler A");

        scoreSpelerBLabel = new Label();
        scoreSpelerBLabel.setTranslateX((sizeRooster *TEGEL_SIZE + 100));
        scoreSpelerBLabel.setTranslateY(sizeRooster *TEGEL_SIZE + 100 + 20);
        scoreSpelerBLabel.setText("Score Speler B");


        aanwieIsHetLabel = new Label();
        aanwieIsHetLabel.setTranslateX(sizeRooster *TEGEL_SIZE + 100);
        aanwieIsHetLabel.setTranslateY((sizeRooster *TEGEL_SIZE + 100)-20);
        aanwieIsHetLabel.setText("tis aan speler A");

        speleradBeurt = State.SPELERA;

        Pane root = new Pane();
        root.setPrefSize((sizeRooster * TEGEL_SIZE) + 200,(sizeRooster * TEGEL_SIZE) + 200);


        root.getChildren().addAll(finishedLabel,scoreSpelerALabel, scoreSpelerBLabel,aanwieIsHetLabel);
        //setten van de offset voor inladen van de fotos
        //      set A zit van 1   tem 42
        //      set B zit van 101 tem 142
        //      set C zit van 201 tem 242

        int offset = 0;
        if(fotoSet == 'B'){offset = 100;}
        else if(fotoSet == 'C'){offset = 300;}


        //generatie van de tegels in een arraylist

        List<Tegel> tegels = new ArrayList<Tegel>();

        for(int i=0 ; i<AANTALPAREN ; i++){
            //tegel met juiste id
            // 2 tegels van elke soort hé motje
            System.out.println("tegel met id: "+i+"inladen");
            tegels.add(new Tegel(i, Main.loadImageBytes(i+offset+1+""), Main.loadImageBytes("0")));
            tegels.add(new Tegel(i, Main.loadImageBytes(i+offset+1+""), Main.loadImageBytes("0")));

        }

        //shuffelen van de tegelslijst
        Collections.shuffle(tegels);


        //voor alle tegels
        for(int i=0 ; i<tegels.size() ; i++){

            Tegel tegel = tegels.get(i);

            //x en y value setten
            tegel.setTranslateX((TEGEL_SIZE+SPACING_TEGEL) * (i % AANTALPERRIJ));
            tegel.setTranslateY((TEGEL_SIZE+SPACING_TEGEL) * (i / AANTALPERRIJ));

            //tegel toevoegen aan de view
            root.getChildren().add(tegel);

        }

        return root;

    }

    //init
    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("start memory zonder parameters");
        primaryStage.setScene(new Scene(createContent()));
        primaryStage.show();
    }

    //init met configuraties
    public void start(Stage primaryStage, int sizeRooster, char fotoSet){

        System.out.println("start memory met parameters triggered");

        Scene scene = new Scene(createContent(sizeRooster, fotoSet));

        primaryStage.setScene(scene);
        primaryStage.show();
    }



    public static void main(String[] args){
        launch(args);
    }



}