package client.Game;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;

/* METHODE WORDT NIET MEER GEBRUIKT */
public class Tegel extends StackPane {

    //attributen
    /*
         - 2 fotos :
                voorkant
                achterkant
         - een Text getal, om ze te onderscheiden van elkaar
     */
    private Rectangle voorkant;
    private Rectangle achterkant; //zal telkens dezelfde zijn
    private int id; //om te vergelijken met andere kaarjes;
    private State state; //state van de kaart achterhalen
    private ScaleTransition stVerbergAchterkant; //om kaart om te draaien, bevat nog een ScaleTransition inwendig
    private ScaleTransition stWaitForReturnOmdraai; //chain van animation die wacht, daarna 2x helft omdraait
    private static final int DUURFLIP = 75; //een totale flip duurt dan 2x zo zolang
    private static final int DUURWAIT = 1000; //duur van wachten alvorens om te draaien in milliseconden

    public Tegel(int id, byte[] voorkantFoto, byte[] achterkantFoto){

        //toekennen van de waarden
        this.id = id;

        state = State.CLOSED;

        voorkant = new Rectangle(100,100);
        achterkant = new Rectangle(100,100);

        //images definen, zetten in de overeenkomstige rectangle
        ImagePattern voorkantImage = new ImagePattern(new Image(new ByteArrayInputStream(voorkantFoto)));
        ImagePattern achterkantImage = new ImagePattern(new Image(new ByteArrayInputStream(achterkantFoto)));

        voorkant.setFill(voorkantImage);
        achterkant.setFill(achterkantImage);
        voorkant.setScaleX(0);

        voorkant.setStroke(Color.BLACK);
        achterkant.setStroke(Color.BLACK);

        //de duur in milliseconden van een HALVE draai!


        //configureren van de animatie om een kaartje om te draaien en de waarde te bekijken dervan
        stVerbergAchterkant = new ScaleTransition(Duration.millis(DUURFLIP), achterkant);
        stVerbergAchterkant.setFromX(1);
        stVerbergAchterkant.setToX(0);

        ScaleTransition stOpenWaarde = new ScaleTransition(Duration.millis(DUURFLIP), voorkant);
        stOpenWaarde.setFromX(0);
        stOpenWaarde.setToX(1);

        stVerbergAchterkant.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stOpenWaarde.play();
            }
        });

        //configureren vd animatie die een kaart terug omdraait zodat we enkel de voorkant zien
        //beginnen met een lange wait
        stWaitForReturnOmdraai = generateOmdraaiChain();




        //alignment en locaties en dergelijke
        setAlignment(Pos.CENTER);
        getChildren().addAll(voorkant,achterkant);

        setOnMouseClicked(this:: handleMouseClick);

    }



    //kaart openen
    public void open(){ //kaart ligt gesloten en we willen er naar kijken
        stVerbergAchterkant.play();
        state = State.OPEN;

    }

    public void close(){


        stWaitForReturnOmdraai.play();
        state = State.CLOSED;
    }

    public boolean isOpen(){
        return state == State.OPEN;
    }

    public boolean isZelfdeTegel(Tegel andere){
        return id == andere.id;
    }

    private void handleMouseClick(MouseEvent mouseEvent) {

        if( isOpen() || MemoryPuzzleApp.clickCount == 0){return;}

        //als we een kaart kunnen aanraken
        MemoryPuzzleApp.clickCount--;

        if(MemoryPuzzleApp.selectedTegel == null){

            //dan geen tegel om mee te vergelijken
            //gewoon de kaart flippen

            MemoryPuzzleApp.selectedTegel = this;
            open();

        }
        else{
            //in selectedTile zit er al een tegel, we moeten dus vergelijken

            open();
            //logica voor de spelers


            if(!isZelfdeTegel(MemoryPuzzleApp.selectedTegel)){ //als het geen identieke tegels zijn

                MemoryPuzzleApp.selectedTegel.close();
                this.close();
            }
            else{ //als wel 2 dezelfde tegels zijn
                MemoryPuzzleApp.AANTALFOUND++;

                if(MemoryPuzzleApp.speleradBeurt == State.SPELERA){
                    MemoryPuzzleApp.scoreSpelerA++;
                    MemoryPuzzleApp.scoreSpelerALabel.setText(MemoryPuzzleApp.scoreSpelerA+"");
                }
                else{
                    MemoryPuzzleApp.scoreSpelerB++;
                    MemoryPuzzleApp.scoreSpelerBLabel.setText(MemoryPuzzleApp.scoreSpelerB+"");
                }



                //als het spelletje klaar is logica
                if(MemoryPuzzleApp.AANTALFOUND == MemoryPuzzleApp.AANTALPAREN){

                    MemoryPuzzleApp.finishedLabel.setVisible(true);
                    //todo: logica als finisched
                }

            }

            //state veranderen
            if(MemoryPuzzleApp.speleradBeurt == State.SPELERA){
                MemoryPuzzleApp.speleradBeurt = State.SPELERB;
                MemoryPuzzleApp.aanwieIsHetLabel.setText("tis aan speler B");
            }
            else{
                MemoryPuzzleApp.speleradBeurt = State.SPELERA;
                MemoryPuzzleApp.aanwieIsHetLabel.setText("tis aan speler A");
            }

            MemoryPuzzleApp.selectedTegel =null;
            MemoryPuzzleApp.clickCount = 2;

        }


    }

    // nie veel op letten, een chain van animaties zodat hij wacht alvorens een kaartje terug om te draaien
    private ScaleTransition generateOmdraaiChain() {

        ScaleTransition stReturner = new ScaleTransition(Duration.millis(DUURWAIT), new Rectangle(1,1));

        ScaleTransition stVerbergVoorkant = new ScaleTransition(Duration.millis(DUURFLIP), voorkant);
        stVerbergVoorkant.setFromX(1);
        stVerbergVoorkant.setToX(0);

        ScaleTransition stOpenAchterkant = new ScaleTransition(Duration.millis(DUURFLIP), achterkant);
        stOpenAchterkant.setFromX(0);
        stOpenAchterkant.setToX(1);

        stVerbergVoorkant.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stOpenAchterkant.play();
            }
        });

        stReturner.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stVerbergVoorkant.play();
            }
        });


        return stReturner;



    }


}