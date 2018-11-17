package client.Controllers;

import Classes.Commando;
import Classes.GameInfo;
import Classes.GameState;
import Classes.Tile;
import client.Game.VisualTile;
import client.Main;
import client.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class SpelViewGui extends Thread {

    @FXML
    private Label gameTitel;

    @FXML
    private Label mijnScoreLabel; private int mijnScore;

    @FXML
    private Label zijnScoreLabel; private int zijnScore;

    @FXML
    private Label aanDeBeurtLabel;

    @FXML
    private Label wachtenLabel;

    @FXML
    private Button leave;

    // variabelen die de game kunnen weergeven hier?

    // vraag: hier een gameState bijhouden , of enkel bijhouden in de appserver
    @FXML
    private AnchorPane overkoepelendePane;

    @FXML
    private AnchorPane root;

    private SpelViewLogica svl;

    private static int TEGEL_SIZE = 100;
    private static int SPACING_TEGEL=10;


    private ArrayList<VisualTile> visualTilesList;



    @FXML
    public void initialize() throws RemoteException, InterruptedException {

        //set label to values in appserver
        int gameId = Main.currentGameId;

        visualTilesList = new ArrayList<VisualTile>();

        System.out.println("debug lijn");
    }

    public void setup(GameInfo gameInfo, GameState gameState){

        // inladen van de game
        setupGame(gameInfo, gameState);

        //disable alle muisclicks, vanaf er een 2de speler bijkomt zal dit terug geenabled worden
        this.disableMouseClick();
    }



    public void enableMouseClick(){
        System.out.println("enable click");
        root.setDisable(false);
        wachtenLabel.setVisible(false);
    }

    public void disableMouseClick(){
        System.out.println("disable click");
        root.setDisable(true);
        wachtenLabel.setVisible(true);
    }

    public boolean isMouseClickEnabled(){
        return !root.isDisabled();
    }


    @FXML
    public void leaveGame(){
        // stop dit
        // stop de thread
        leave.getScene().getWindow().hide();

        //todo: fix die juiste stop ier tho, das lijk nog nen belangrijken

        Main.goToLobby();
    }


    private void setupGame(GameInfo gameInfo, GameState gameState) {

        //todo: breid deze methode uit zodat het voor alle gamestates werkt, en niet enkel de initielegamestate
        if(gameInfo.getRoosterSize() == 4){
            overkoepelendePane.setPrefSize(600,600);
            root.setPrefSize(460,460);
        }
        else{
            overkoepelendePane.setPrefSize(694,750);
            root.setPrefSize(680,680);}

        int aantalParen = gameState.getAantalParen();
        int aantalPerRij = gameState.getAantalPerRij();

        for (int i = 0; i < gameState.getTegelsList().size(); i++) {

            VisualTile tile = new VisualTile(gameState.getTegelsList().get(i));

            tile.setTranslateX((TEGEL_SIZE+SPACING_TEGEL) * (i % aantalPerRij));
            tile.setTranslateY((TEGEL_SIZE+SPACING_TEGEL) * (i / aantalPerRij));

            //opgeslaan in deze arrayList zodat we ze nog kunnen laten flippen op commando
            visualTilesList.add(tile);


            root.getChildren().add(tile);


        }
        for (int i = 0; i < gameState.getTegelsList().size(); i++) {
            Tile tile= gameState.getTegelsList().get(i);
            if(tile.isFlippedOver()){
                visualTilesList.get(i).flip();
            }
        }


        if(User.getCurrentUser().getUsername().equals(gameInfo.getClientA())){
            mijnScore = gameState.getAantalPuntenSpelerA();
            zijnScore = gameState.getAantalPuntenSpelerB();
        }
        else{
            zijnScore = gameState.getAantalPuntenSpelerA();
            mijnScore = gameState.getAantalPuntenSpelerB();
        }
        mijnScoreLabel.setText(Integer.toString(mijnScore));
        zijnScoreLabel.setText(Integer.toString(zijnScore));

    }

    public void executeCommando(Commando commando){

        int uniqueTileId = commando.getUniqueTileId();

        VisualTile deTile = getTileMetUniqueId(uniqueTileId, visualTilesList);
        //execute het commando
        if(commando.getType().equals("FLIP")){

            deTile.flip();

        }
        else if(commando.getType().equals("UNFLIP")){

            deTile.unflip();

        }
        else if(commando.getType().equals("SWITCH")){

            if(isMouseClickEnabled()){
                disableMouseClick();
            }
            else{enableMouseClick();}

        }
        else if(commando.getType().equals("LOCK")){

            System.out.println("disabling tile met nummer :"+uniqueTileId);
            deTile.setDisable(true);

        }

        else if(commando.getType().equals("UNLOCK")){
            deTile.setDisable(false);
        }
        else if(commando.getType().equals("AWARDTOME")){
            mijnScore++;
            mijnScoreLabel.setText(mijnScore+"");

        }
        else if(commando.getType().equals("AWARDTOYOU")){
            zijnScore++;
            zijnScoreLabel.setText(zijnScore+"");
        }
        else if(commando.getType().equals("WIN")){
            disableMouseClick();
            wachtenLabel.setText("WINNER WINNER CHICKEN DINNER");
        }

        else if(commando.getType().equals("LOSS")){
            disableMouseClick();
            wachtenLabel.setText("LOSER");
        }

        else if(commando.getType().equals("DRAW")){
            disableMouseClick();
            wachtenLabel.setText("GELIJKSPEL");
        }

        else{

            System.out.println("fout in verwerkCommando's: SpelViewGui.java");
            System.out.println("fout in verwerking, instructie was niet FLIP of UNFLIP");

        }


    }


    private VisualTile getTileMetUniqueId(int uniqueTileId, ArrayList<VisualTile> visualTilesList) {

        for (VisualTile visualTile : visualTilesList) {
            if(visualTile.getUniqueId() == uniqueTileId){return visualTile;}
        }

        System.out.println("fout in verwerkCommando's: SpelViewGui.java");
        System.out.println("fout in getTileMetUniqueId: tile met gevraagde id wordt niet gevonden");
        return null;
    }


    public void setLogic(SpelViewLogica spelViewLogica) {
        this.svl=spelViewLogica;
    }
}
