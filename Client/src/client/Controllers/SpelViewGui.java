package client.Controllers;

import Classes.Commando;
import Classes.GameInfo;
import Classes.GameState;
import Classes.Tile;
import client.CurrentGame;
import client.Game.VisualTile;
import client.Main;
import client.CurrentUser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class SpelViewGui extends Thread {

    @FXML
    private Label gameTitel;

    // TODO make scorelabel in fxml
    @FXML
    private Label scoreLabel;
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
    public AnchorPane root;

    private SpelViewLogica svl;

    private static int TEGEL_SIZE = 100;
    private static int SPACING_TEGEL=10;


    private ArrayList<VisualTile> visualTilesList;



    @FXML
    public void initialize() throws RemoteException, InterruptedException {

        //set label to values in appserver
        visualTilesList = new ArrayList<VisualTile>();

        System.out.println("debug lijn");
    }

    public void setup(GameInfo gameInfo, GameState gameState){

        // inladen van de game
        setupGame(gameInfo, gameState);

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
        //todo: fix die juiste stop ier tho, das lijk nog nen belangrijken
        svl.leave();

        leave.getScene().getWindow().hide();


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


        if(gameInfo.getAantalSpelersConnected()==gameState.getAantalSpelers()) {
            if (gameState.getAandeBeurt().equals(CurrentUser.getInstance().getUsername())) {
                System.out.println("welkom terug, jouw beurt");
                enableMouseClick();
            } else {
                System.out.println("welkom terug, niet jouw beurt");
                disableMouseClick();
            }
        }
        else{
            System.out.println("welkom terug, 2de speler is weg");
            disableMouseClick();
        }


       visualiseerPunten();



        //extra logica zodat finished game niet meer clickable is
        if(gameState.getfinished()){
            System.out.println("tibotibo game is finished");
            //lock alles
            root.setDisable(true);
        }

    }

    public void executeCommando(Commando commando) throws RemoteException {

        int uniqueTileId = commando.getUniqueTileId();

        VisualTile deTile = getTileMetUniqueId(uniqueTileId, visualTilesList);

        String commandoMessage = commando.getType();
        //execute het commando
        if(commandoMessage.equals("FLIP")){

            deTile.flip();

        }
        else if(commandoMessage.equals("UNFLIP")){

            deTile.unflip();

        }
        else if(commandoMessage.equals("SWITCH")){

            //TODO ik denk dat dit commando weg mag met huidige implementatie
            //todo : tis waaar éja, grts tibo


        }
        else if(commandoMessage.equals("LOCK")){

            //System.out.println("disabling tile met nummer :"+uniqueTileId);
            deTile.setDisable(true);

        }

        else if(commandoMessage.equals("UNLOCK")){
            deTile.setDisable(false);
        }
        else if(commandoMessage.equals("AWARD")){

            String user= commando.getEffectOnUser();
            CurrentGame.getInstance().getGameState().getPunten().put(user, CurrentGame.getInstance().getGameState().getPunten().get(user)+1);
            visualiseerPunten();

        }
        else if(commandoMessage.equals("WIN")){
            disableMouseClick();
            wachtenLabel.setText("WINNER WINNER CHICKEN DINNER");

            String username = CurrentUser.getInstance().getUsername();
            int eindScore = CurrentGame.getInstance().getGameState().getPunten().get(username);
            int roosterSize = CurrentGame.getInstance().getGameInfo().getRoosterSize();


            Main.cnts.getAppImpl().updateScores(username, roosterSize, eindScore, "WIN");
        }

        else if(commandoMessage.equals("LOSS")){
            disableMouseClick();
            wachtenLabel.setText("LOSER");

            String username = CurrentUser.getInstance().getUsername();
            int roosterSize = CurrentGame.getInstance().getGameInfo().getRoosterSize();
            int eindScore = CurrentGame.getInstance().getGameState().getPunten().get(username);
            Main.cnts.getAppImpl().updateScores(username, roosterSize, eindScore, "LOSS");
        }

        else if(commandoMessage.equals("DRAW")){
            disableMouseClick();
            wachtenLabel.setText("GELIJKSPEL");

            String username = CurrentUser.getInstance().getUsername();
            int roosterSize = CurrentGame.getInstance().getGameInfo().getRoosterSize();
            int eindScore = CurrentGame.getInstance().getGameState().getPunten().get(username);
            Main.cnts.getAppImpl().updateScores(username, roosterSize, eindScore, "DRAW");
        }

        else{

            System.out.println("fout in verwerkCommando's: SpelViewGui.java");
            System.out.println("fout in verwerking, instructie was niet FLIP of UNFLIP");

        }

        if(commandoMessage.equals("WIN") || commandoMessage.equals("LOSS") || commandoMessage.equals("DRAW")){

            //todo: fix dat je de game connecties lijk afrondt
            try {
                Main.cnts.getDispatchImpl().gameFinished();
            } catch (RemoteException e) {
                e.printStackTrace();
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


    public void visualiseerPunten(){
        StringBuilder score= new StringBuilder();
        for (String speler : CurrentGame.getInstance().getGameState().getSpelers()) {
            score.append(speler+": "+ CurrentGame.getInstance().getGameState().getPunten().get(speler)+"\n");
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                scoreLabel.setText(score.toString());
            }
        });

    }

    public void setLogic(SpelViewLogica spelViewLogica) {
        this.svl=spelViewLogica;
    }

    public void spectatorMode() {
        overkoepelendePane.setDisable(true);
    }

    public void setLabelOp(String spectate) {
        wachtenLabel.setText(spectate);
    }

    public void closeWithoutLeave() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                scoreLabel.getScene().getWindow().hide();
            }
        });
    }
}
