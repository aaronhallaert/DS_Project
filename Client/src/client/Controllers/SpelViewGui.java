package client.Controllers;

import Classes.Commando;
import Classes.GameInfo;
import Classes.GameState;
import client.Game.VisualTile;
import client.Main;
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

    private static int TEGEL_SIZE = 100;
    private static int SPACING_TEGEL=10;

    private GameInfo gameInfo;

    private ArrayList<VisualTile> visualTilesList;



    @FXML
    public void initialize() throws RemoteException, InterruptedException {

        //set label to values in appserver
        int gameId = Main.currentGameId;

        visualTilesList = new ArrayList<VisualTile>();


        gameInfo = Main.cnts.getAppImpl().getGameInfo(Main.currentGameId);
        //checken als het wel kan, als de default gameConstructor() is gereturned, dan is er een fout van de gameId
        if(gameInfo.getGameId() == 0){
            gameTitel.setText("fout in gameId!");
        }
        else{// bij successvolle create van een game

            GameState initieleGameState = Main.cnts.getAppImpl().getGameSate(gameId);

            // inladen van de game
            setupInitieleGame(gameInfo, initieleGameState);

            //disable alle muisclicks
            this.disableMouseClick();

            //tot hier mag weg: is een voorbeeldje

        }

        System.out.println("debug lijn");
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


    private void setupInitieleGame(GameInfo gameInfo, GameState initieleGameState) {

        //todo: breid deze methode uit zodat het voor alle gamestates werkt, en niet enkel de initielegamestate
        if(gameInfo.getRoosterSize() == 4){
            overkoepelendePane.setPrefSize(600,600);
            root.setPrefSize(460,460);
        }
        else{
            overkoepelendePane.setPrefSize(694,750);
            root.setPrefSize(680,680);}

        int aantalParen = initieleGameState.getAantalParen();
        int aantalPerRij = initieleGameState.getAantalPerRij();

        for (int i = 0; i < initieleGameState.getTegelsList().size(); i++) {

            VisualTile tile = new VisualTile(initieleGameState.getTegelsList().get(i));

            tile.setTranslateX((TEGEL_SIZE+SPACING_TEGEL) * (i % aantalPerRij));
            tile.setTranslateY((TEGEL_SIZE+SPACING_TEGEL) * (i / aantalPerRij));

            //opgeslaan in deze arrayList zodat we ze nog kunnen laten flippen op commando
            visualTilesList.add(tile);


            root.getChildren().add(tile);


        }

        mijnScore = 0;
        zijnScore = 0;
        mijnScoreLabel.setText("0");
        zijnScoreLabel.setText("0");

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



    //todo: remove this
    // als je commandos binnenkrijgt van de appserver, om ze te executen
    public void executeCommandos(ArrayList<Commando> commandoLijst){

        for (int i = 0; i < commandoLijst.size(); i++) {

            Commando commando = commandoLijst.get(i);
            int uniqueTileId = commando.getUniqueTileId();

            VisualTile deTile = getTileMetUniqueId(uniqueTileId, visualTilesList);
            //execute het commando
            if(commando.getType().equals("FLIP")){

                deTile.flip();

            }
            else if(commando.getType().equals("UNFLIP")){

                deTile.unflip();

            }
            else{

                System.out.println("fout in verwerkCommando's: SpelViewGui.java");
                System.out.println("fout in verwerking, instructie was niet FLIP of UNFLIP");

            }


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


}
