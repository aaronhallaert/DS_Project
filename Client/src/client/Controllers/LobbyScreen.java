package client.Controllers;

import Classes.GameInfo;
import client.CurrentGame;
import client.GameObs;
import client.Main;
import client.SupportiveThreads.LobbyRefreshThread;
import client.CurrentUser;
import com.sun.corba.se.spi.orbutil.fsm.GuardBase;
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
    public Label joinErrorLabel;
    private RotateTransition errorAnimation;

    /* table en al zijn columns */
    @FXML
    public TableView<GameObs> activeGamesTable;

    @FXML
    public TableColumn<GameObs, String> gameIdColumn;

    @FXML
    public TableColumn<GameObs, Integer> totaalSpelersColumn;

    @FXML
    public TableColumn<GameObs, Integer> aantalJoinedColumn;

    @FXML
    public TableColumn<GameObs, String> fotoSetColumn;

    @FXML
    public TableColumn<GameObs, Integer> roosterSizeColumn;

    @FXML
    public TableColumn<GameObs, List<String>> spelerNamenColumn;

    public static ArrayList<GameInfo> gameInfoList;
    public static ObservableList<GameObs> gamesObsList;


    @FXML
    public void initialize(){

        try {
            // eerste maal de gamesLijst van de appserver halen en visualiseren en de datatable
            gameInfoList = Main.cnts.getAppImpl().getGameInfoLijst();

            gamesObsList = Main.configureList(gameInfoList);


            //waarden van de tabel invullen

            //deze strings zijn de exacte attribuutnamen van GameObs
            gameIdColumn.setCellValueFactory(new PropertyValueFactory<>("gameId"));
            aantalJoinedColumn.setCellValueFactory(new PropertyValueFactory<>("aantalSpelerConnected"));
            totaalSpelersColumn.setCellValueFactory(new PropertyValueFactory<>("maxAantalSpelers"));
            fotoSetColumn.setCellValueFactory(new PropertyValueFactory<>("fotoSet"));
            roosterSizeColumn.setCellValueFactory(new PropertyValueFactory<>("roosterSize"));
            spelerNamenColumn.setCellValueFactory(new PropertyValueFactory<>("spelers"));

            // add data to table
            activeGamesTable.setItems(gamesObsList);

            // thread die om de 5 seconden de lobbytafel refresht aanmaken + opstarten
            LobbyRefreshThread checkAvailableGames= new LobbyRefreshThread(this);
            checkAvailableGames.start();

            joinErrorLabel.setVisible(false);
            //shaketransition configureren
            errorAnimation = new RotateTransition(Duration.millis(50), joinErrorLabel);
            errorAnimation.setByAngle(10);
            errorAnimation.setCycleCount(8);
            errorAnimation.setAutoReverse(true);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void refresh(){
        activeGamesTable.setItems(gamesObsList);
    }

    public void logout(){
        try {
            Main.cnts.getDispatchImpl().logoutUser(CurrentUser.getInstance().getUsername());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Main.goToLogin();
        // Hide this current window
        logoutLink.getScene().getWindow().hide();
    }

    @FXML
    public void spelSetup(){

        //huidig window wegdoen
        logoutLink.getScene().getWindow().hide();

        //naar volgend window
        Main.goToSetupSpel();
    }

    @FXML
    private void joinGame() {

        GameObs deGameToJoin = activeGamesTable.getSelectionModel().getSelectedItem();
        if(deGameToJoin == null){

            //geef iets van info dat je geen game gekozen hebt
            System.out.println("er bestaat geen dergelijke game");
            displayErrorMessage("Geen game aangeklikt!");

        }
        else {

            //join this game
            int currentGameIdAttempt = deGameToJoin.getGameId();

            //try to join
            try {
                if(Main.cnts.getAppImpl().join(CurrentUser.getInstance().getUsername(), currentGameIdAttempt)){
                    CurrentGame.setInstance(Main.cnts.getAppImpl().getGame(currentGameIdAttempt));

                    // ga verder naar GAME
                    SpelViewLogica spv = new SpelViewLogica(true);
                    spv.start();

                    Platform.setImplicitExit(false);
                    spelSetup.getScene().getWindow().hide();
                }
                else{ // als geen successvolle join

                    displayErrorMessage("join failed");

                    System.out.println("lobbyscreen.java: je bent een 3 e speler die probeert te joinen en dat mag niet!");

                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }




    }

    @FXML
    public void spectate(){

        GameObs deGameToJoin = activeGamesTable.getSelectionModel().getSelectedItem();
        if(deGameToJoin == null){

            //geef iets van info dat je geen game gekozen hebt
            System.out.println("er bestaat geen dergelijke game");
            displayErrorMessage("Geen game aangeklikt!");

        }
        else {

            //join this game
            int currentGameIdAttempt = deGameToJoin.getGameId();

            //try to join
            try {

                CurrentGame.setInstance(Main.cnts.getAppImpl().getGame(currentGameIdAttempt));

                String thisUser  = CurrentUser.getInstance().getUsername();

                Main.cnts.getAppImpl().spectate(CurrentGame.getInstance().getGameState().getGameId(), CurrentUser.getInstance().getUsername());

                if(!CurrentGame.getInstance().getGameInfo().getSpelers().contains(thisUser)) {
                    // ga verder naar spectaten ALLEEN als je niet deelneemt aan de game zelf
                    SpelViewLogica spv = new SpelViewLogica(false);
                    spv.start();
                    //dit gaat fout want het zal ontlocked worden waarschijnlijk later

                    Platform.setImplicitExit(false);
                    spelSetup.getScene().getWindow().hide();
                }

                else{
                    displayErrorMessage("eigen game niet joinbaar!");
                }

            }

            catch (RemoteException e) {
                e.printStackTrace();
            }

        }


    }

    private void displayErrorMessage(String message) {
        joinErrorLabel.setText(message);
        joinErrorLabel.setVisible(true);
        errorAnimation.play();
    }





}
