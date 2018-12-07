package client.Controllers;

import Classes.GameInfo;
import client.CurrentGame;
import client.GameInfoObs;
import client.Main;
import client.SupportiveThreads.LobbyRefreshThread;
import client.CurrentUser;
import interfaces.AppServerInterface;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class LobbyScreen {

    @FXML
    Hyperlink logoutLink;

    @FXML
    Button createGame;

    @FXML
    Button spelSetup;

    @FXML
    Button spectateButton;

    @FXML
    Button joinButton;

    @FXML
    Button toScoreScreen;

    @FXML
    public Label joinErrorLabel;
    private RotateTransition errorAnimation;

    /* table en al zijn columns */
    @FXML
    public TableView<GameInfoObs> activeGamesTable;

    @FXML
    public TableColumn<GameInfoObs, String> gameIdColumn;

    @FXML
    public TableColumn<GameInfoObs, Integer> totaalSpelersColumn;

    @FXML
    public TableColumn<GameInfoObs, Integer> aantalJoinedColumn;

    @FXML
    public TableColumn<GameInfoObs, String> fotoSetColumn;

    @FXML
    public TableColumn<GameInfoObs, Integer> roosterSizeColumn;

    @FXML
    public TableColumn<GameInfoObs, List<String>> spelerNamenColumn;

    public static ArrayList<GameInfo> gameInfoList;
    public static ObservableList<GameInfoObs> gamesObsList;
    private LobbyRefreshThread checkAvailableGames;

    @FXML
    public void initialize(){

        try {


            /*---------- INIT SCORES ---------------*/
            //todo remove this , gewoon omdat we met verschillende databases werken dat het een probleem kan geven anders
            try {
                Main.cnts.getAppImpl().checkIfHasScoreRowAndAddOneIfHasnt(CurrentUser.getInstance().getUsername());
            } catch (RemoteException e) {
                e.printStackTrace();
            }



            /*---------- GAMEINFO TABLE -------------*/

            // eerste maal de gamesLijst van de appserver halen en visualiseren en de datatable
            gameInfoList = Main.cnts.getAppImpl().getGameInfoLijst();
            gamesObsList = Main.configureList(gameInfoList);

            //waarden van de tabel invullen
            gameIdColumn.setCellValueFactory(new PropertyValueFactory<>("gameId"));
            aantalJoinedColumn.setCellValueFactory(new PropertyValueFactory<>("aantalSpelerConnected"));
            totaalSpelersColumn.setCellValueFactory(new PropertyValueFactory<>("maxAantalSpelers"));
            fotoSetColumn.setCellValueFactory(new PropertyValueFactory<>("fotoSet"));
            roosterSizeColumn.setCellValueFactory(new PropertyValueFactory<>("roosterSize"));
            spelerNamenColumn.setCellValueFactory(new PropertyValueFactory<>("spelers"));

            // add data to table
            activeGamesTable.setItems(gamesObsList);

            // refresh lobby bij verandering
            checkAvailableGames= new LobbyRefreshThread(this);
            checkAvailableGames.start();



            /*------------ INIT LAYOUT STATE --------------*/
            joinErrorLabel.setVisible(false);



            /*---------- ANIMATIONS --------------*/
            errorAnimation = new RotateTransition(Duration.millis(50), joinErrorLabel);
            errorAnimation.setByAngle(10);
            errorAnimation.setCycleCount(8);
            errorAnimation.setAutoReverse(true);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * refreshen van de game info table
     */
    public void refresh(){
        activeGamesTable.setItems(gamesObsList);
    }

    @FXML
    public void logout(){
        try {
            Main.cnts.getAppImpl().logoutUser(CurrentUser.getInstance().getUsername());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // GUI
        Main.goToLogin();
        logoutLink.getScene().getWindow().hide();
    }

    @FXML
    public void spelSetup(){
        // GUI
        logoutLink.getScene().getWindow().hide();
        Main.goToSetupSpel();
    }

    @FXML
    private void joinGame() {

        // selectie opvragen
        GameInfoObs deGameToJoin = activeGamesTable.getSelectionModel().getSelectedItem();
        if(deGameToJoin == null){
            displayErrorMessage("Geen game geselecteerd!");
        }
        else {

            // id van game die we proberen te joinen
            int currentGameIdAttempt = deGameToJoin.getGameId();
            try {
                if(Main.cnts.getAppImpl().hasGame(currentGameIdAttempt)) {
                    // als application server die momenteel verbonden is met deze client de game ter beschikking heeft,
                    // kan men proberen de game te joinen
                    if (Main.cnts.getAppImpl().join(CurrentUser.getInstance().getUsername(), currentGameIdAttempt)) {

                        // SET GAME //
                        CurrentGame.setInstance(Main.cnts.getAppImpl().getGame(currentGameIdAttempt));

                        // GUI //
                        SpelViewLogica spv = new SpelViewLogica(true);
                        spv.start();
                        Platform.setImplicitExit(false);
                        spelSetup.getScene().getWindow().hide();


                    } else {
                        displayErrorMessage("Join Failed");
                    }



                }
                else{
                    /*------- OPVRAGEN NIEUWE APPSERVER --------*/
                    //TODO wat als er geen enkele appserver deze game heeft?
                    checkAvailableGames.stop();
                    AppServerInterface newServer= Main.cnts.getDispatchImpl().changeClientServer(currentGameIdAttempt);
                    if(newServer!=null) {
                        Main.cnts.setAppImpl(newServer);
                    }
                    checkAvailableGames= new LobbyRefreshThread(this);
                    checkAvailableGames.start();
                    joinGame();

                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }




    }

    @FXML
    public void spectate(){

        // selectie opvragen
        GameInfoObs deGameToJoin = activeGamesTable.getSelectionModel().getSelectedItem();
        if(deGameToJoin == null){
            displayErrorMessage("Geen game aangeklikt!");
        }
        else {

            // id van game dat we proberen te spectaten
            int currentGameIdAttempt = deGameToJoin.getGameId();


            try {
                // men kan enkel spectaten bij de appserver waar game draait
                if(Main.cnts.getAppImpl().hasGame(currentGameIdAttempt)) {
                    CurrentGame.setInstance(Main.cnts.getAppImpl().getGame(currentGameIdAttempt));
                    String thisUser = CurrentUser.getInstance().getUsername();


                    if (!CurrentGame.getInstance().getGameInfo().getSpelers().contains(thisUser)) {
                        //registratie om te spectaten bij appserver
                        Main.cnts.getAppImpl().spectate(CurrentGame.getInstance().getGameState().getGameId(), CurrentUser.getInstance().getUsername());

                        // ga verder naar spectaten ALLEEN als je niet deelneemt aan de game zelf
                        SpelViewLogica spv = new SpelViewLogica(false);
                        spv.start();

                        Platform.setImplicitExit(false);
                        spelSetup.getScene().getWindow().hide();
                    } else {
                        displayErrorMessage("eigen game niet joinbaar!");
                    }
                }
                else{
                    /*------- OPVRAGEN NIEUWE APPSERVER --------*/
                    //TODO wat als er geen enkele appserver deze game heeft?
                    checkAvailableGames.stop();
                    AppServerInterface newServer= Main.cnts.getDispatchImpl().changeClientServer(currentGameIdAttempt);
                    if(newServer!=null) {
                        Main.cnts.setAppImpl(newServer);
                    }
                    checkAvailableGames= new LobbyRefreshThread(this);
                    checkAvailableGames.start();
                    // probeer opnieuw te spectaten
                    spectate();
                }

            }

            catch (RemoteException e) {
                e.printStackTrace();
            }

        }


    }

    @FXML
    public void goToScoreScreen() {
        Main.goToScoreScreen();

        Platform.setImplicitExit(false);
        spelSetup.getScene().getWindow().hide();

    }

    private void displayErrorMessage(String message) {
        joinErrorLabel.setText(message);
        joinErrorLabel.setVisible(true);
        errorAnimation.play();
    }

}
