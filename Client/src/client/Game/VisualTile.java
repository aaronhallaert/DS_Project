package client.Game;

import Classes.Commando;
import Classes.Tile;
import client.CurrentGame;
import client.Main;
import client.CurrentUser;
import javafx.animation.ScaleTransition;
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
import java.rmi.RemoteException;

public class VisualTile extends StackPane {

    private Rectangle voorkant; // hierin komt de afbeelding
    private Rectangle achterkant; //zal telkens dezelfde zijn
    private int uniqueId; //deze is voor elke tegel anders                                  analoog aan Tile
    private int id; //om te vergelijken met andere kaarjes, 2 tegels hebben dezelfde id     analoog aan Tile
    private ScaleTransition stVerbergAchterkant; //animatie die de kaart omdraait naar open
    private ScaleTransition stVerbergVoorkant; //chain van animations die de kaart terug naar gesloten omdraait

    //duur van animaties
    private static final int DUURFLIP = 75; //een totale flip duurt dan 2x zo zolang
    private static final int DUURWAIT = 1000; //duur van wachten alvorens om te draaien in milliseconden

    //grootte van een tile
    private static final int ZIJDETILE = 100;

    //rand van een tile kleur
    private static final Color KLEURRAND = Color.BLACK;

    /**
     * krijgt een Tile van de appserver en maakt daar lokaal een VisualTile mee
     * @param tile
     */
    public VisualTile(Tile tile){

        //toekennen van de waarden
        this.uniqueId = tile.getUniqueIdentifier();
        this.id = tile.getId();

        voorkant = new Rectangle(ZIJDETILE,ZIJDETILE);
        achterkant = new Rectangle(ZIJDETILE,ZIJDETILE);

        //images definen zetten in de overeenkomstige rectangel
        byte[] voorkantFoto = Main.loadImageBytes(tile.getImageId());
        byte[] achterkantFoto = Main.loadImageBytes(tile.getBackImageId());

        ImagePattern voorkantImage = new ImagePattern(new Image(new ByteArrayInputStream(voorkantFoto)));
        ImagePattern achterkantImage = new ImagePattern(new Image(new ByteArrayInputStream(achterkantFoto)));

        voorkant.setFill(voorkantImage);
        achterkant.setFill(achterkantImage);

        voorkant.setScaleX(0);

        //randen van de Tile invullen
        voorkant.setStroke(KLEURRAND);
        achterkant.setStroke(KLEURRAND);

        //configureren van de animatie om een kaartje om te draaien en de waarde te bekijken dervan
        stVerbergAchterkant = generateAchterkantFlipChain();

        //configureren van de animatie om een kaartje terug te draaien naar gesloten kant
        stVerbergVoorkant = generateVoorkantFlipChain();

        setAlignment(Pos.CENTER);
        getChildren().addAll(voorkant, achterkant);

        setOnMouseClicked(this:: handleMouseClick);
    }

    //commando's nodig zodat we instructies van de appserver kunnen uitvoeren

    //FLIPT ZODAT WE DE FOTO KUNNEN ZIEN
    public void flip(){
        stVerbergAchterkant.play();
    }

    //FLIPT TERUG NAAR DE YUGIOH ACHTERKANT
    public void unflip(){

    }


    /**
     * klikken op een kaartje:
     *          draait het kaartje om en geeft de info door aan de appserver
     *
     */
    private void handleMouseClick(MouseEvent mouseEvent) {
        //System.out.println("tile clicked");
        stVerbergAchterkant.play();
        geefFlipCommandoDoorAanAppServer(this.getUniqueId());
    }

    /**
     * maakt lokaal een commando aan en stuurt het naar de appserver, daar wordt er beslist wat er met dit commando
     * moet gebeuren
     * @param uniqueId
     */
    private void geefFlipCommandoDoorAanAppServer(int uniqueId) {
        Commando commando = new Commando("FLIP",uniqueId);
        try {
            // Main.cnts.getAppImpl().executeFlipCommando(commando, CurrentUser.getInstance().getUsername(), CurrentGame.getInstance().getGameId());
            Main.cnts.getAppImpl().executeFlipCommando(commando, CurrentUser.getInstance().getUsername(), CurrentGame.getInstance().getGameId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    public int getUniqueId(){return uniqueId;}


    //configureren van de animatie om een kaartje om te draaien en de waarde te bekijken dervan
    private ScaleTransition generateAchterkantFlipChain() {
        ScaleTransition sct = new ScaleTransition(Duration.millis(DUURFLIP), achterkant);
        sct.setFromX(1);
        sct.setToX(0);

        ScaleTransition stOpenWaarde = new ScaleTransition(Duration.millis(DUURFLIP), voorkant);
        stOpenWaarde.setFromX(0);
        stOpenWaarde.setToX(1);

        sct.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stOpenWaarde.play();
            }
        });

        return sct;
    }

    //configureren van de animatie om een kaartje terug te draaien naar gesloten kant
    private ScaleTransition generateVoorkantFlipChain() {

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
