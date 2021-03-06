package client.Controllers;

import Classes.GameInfo;
import Classes.GameState;
import client.CurrentGame;
import client.Main;
import client.SupportiveThreads.ReceiveThread;
import client.SupportiveThreads.WaitOnTurn;
import client.SupportiveThreads.WaitPlayerThread;
import client.CurrentUser;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class SpelViewLogica extends Thread{

    private final boolean playerMode;
    private SpelViewGui spvGui;

    //lokale gameinfo
    private GameInfo gameInfo;
    private GameState gameState;

    private boolean disconnected=false;

    private Thread waitPlayer;
    private Thread waitTurn;
    private Thread receiveThread;



    public SpelViewLogica(boolean playerMode) {
        this.playerMode= playerMode;
    }


    @Override
    public void run() {



        try {
            //lokale gameinfo halen uit "echte" gameInfo

            gameInfo= CurrentGame.getInstance().getGameInfo();
            gameState= CurrentGame.getInstance().getGameState();


            System.out.println("STARTUP GAME; voorlopig zijn er "+gameInfo.getAantalSpelersConnected()+" spelers aanwezig");

            if(playerMode) {
                // in deze thread wacht men op een 2de speler
                waitPlayer = new WaitPlayerThread(this, gameInfo);
                waitPlayer.start();


                waitTurn = new WaitOnTurn(this, gameInfo, gameState);
                waitTurn.start();
            }

            // laden van screen en tegels
            spvGui = loadAndSetGui();

            if(playerMode) {
                // ALS HET AAN MIJN BEURT IS DAN MAGK KLIKKEN EN ALS ER 2 SPELERS GECONNECTEERD ZIJN
                if (CurrentGame.getInstance().getGameInfo().getAantalSpelersConnected() == CurrentGame.getInstance().getGameState().getAantalSpelers()) {


                    if (CurrentGame.getInstance().getGameState().getAandeBeurt().equals(CurrentUser.getInstance().getUsername())) {
                        myTurn(true);
                    } else {
                        myTurn(false);
                    }
                } else {
                    spvGui.disableMouseClick();
                }
            }
            else{
                spvGui.setLabelOp("SPECTATE");
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }


        //in deze thread voert men de commando's uit op de andere speler zijn ding
        receiveThread = new ReceiveThread(CurrentUser.getInstance().getUsername(), CurrentGame.getInstance().getGameId(), spvGui);
        receiveThread.start();







    }

    /**
     * deze methode zorgt ervoor dat er niet geklikt kan worden op de kaarten als de 2de speler niet aanwezig is
     * indien ze dan toch beide aanwezig zijn, wordt eerst nog gecontroleerd of het wel "mijn" beurt is
     */
    public void bothPlayersConnected(boolean b){

        if(CurrentGame.getInstance().getGameState().getAandeBeurt().equals(CurrentUser.getInstance().getUsername())){
            if(b) {
                spvGui.enableMouseClick();
            }
            else{
                spvGui.disableMouseClick();
            }
        }
        else{
            spvGui.disableMouseClick();
        }

    }



    /** Laadt de gui, voert de initialize methode al uit. wacht hier op
     *
     * @return de instantie van de controller van de gui. deze is nodig om dan abstracte omdraaimethoden uit te voeren
     * @throws RemoteException
     */
    private SpelViewGui loadAndSetGui() throws RemoteException {

        System.out.println("load and set gui");
        Parent root = null;
        SpelViewGui controller =null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/spelViewGui.fxml"));
            root = fxmlLoader.load();
            controller = fxmlLoader.getController();
            controller.setLogic(this);


        } catch (IOException e) {
            e.printStackTrace();
        }

        Parent finalRoot = root;
        SpelViewGui finalController = controller;
        Platform.runLater(
                () -> {
                    Stage spelViewStage= new Stage();
                    spelViewStage.setTitle("memory spel");
                    //width en height van de scene bepalen
                    //dit moet hier geset worden, jammergenoeg, we kunnen dit niet later aanpassen

                    ArrayList<Integer> values = null;
                    try {
                        values = bepaaldAfmetingenGameScreen();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    Scene startScene= new Scene(finalRoot,values.get(0) , values.get(1)); //misschien nog wijzigen
                    spelViewStage.setScene(startScene);
                    spelViewStage.setResizable(false);
                    spelViewStage.show();
                    spelViewStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        public void handle(WindowEvent we) {
                           leave();
                        }});

                    finalController.setup(gameInfo, gameState);
                }
        );





        //setten van de controller variable

        return controller;
    }

    public void leave(){
        try {
            if(playerMode) {
                waitPlayer.stop();
                waitTurn.stop();
                Main.cnts.getAppImpl().leaveGame(CurrentGame.getInstance().getGameInfo().getGameId(), CurrentUser.getInstance().getUsername());
            }
            else{
                Main.cnts.getAppImpl().unsubscribeSpecator(CurrentGame.getInstance().getGameId(), CurrentUser.getInstance().getUsername());
            }
            receiveThread.stop();
            CurrentGame.resetGame();
            //currentThread().stop();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    /**
     * screen size variabel afhankelijk van de grootte van het rooster
     * moet apart bepaald worden want we kunnen dat niet setten, read onli
     * @return
     * @throws RemoteException
     */
    private static ArrayList<Integer> bepaaldAfmetingenGameScreen() throws RemoteException {

        int width, height = 0;

        if(Main.cnts.getAppImpl().getGameInfo(CurrentGame.getInstance().getGameId()).getRoosterSize() == 4){
            System.out.println("game van 4X4");
            width = 480;
            height = 520;
        }
        else{
            System.out.println("game van 6X6");
            width = 700;
            height = 750;

        }

        ArrayList<Integer> values = new ArrayList<Integer>();
        values.add(width);
        values.add(height);
        return values;

    }

    public void myTurn(boolean b) {
        if(b){
            spvGui.enableMouseClick();
        }
        else{
            spvGui.disableMouseClick();
        }
    }

    public void spectatorMode() {
        spvGui.spectatorMode();
    }

    public void updateScore() {

        spvGui.visualiseerPunten();


    }

    public void updateGame() {
        try {
            CurrentGame.setInstance(Main.cnts.getAppImpl().getGame(CurrentGame.getInstance().getGameId()));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void fixDisconnection() {

        spvGui.closeWithoutLeave();
        if(!disconnected){
            if(playerMode) {
                waitPlayer.stop();
                waitTurn.stop();
            }
            receiveThread.stop();
            CurrentGame.resetGame();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Main.goToDisconnection(spvGui.root.getScene());
                }
            });
            disconnected=true;
        }


    }


}
