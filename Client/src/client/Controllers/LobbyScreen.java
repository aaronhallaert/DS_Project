package client.Controllers;

import Classes.GameInfo;
import client.CurrentGame;
import client.GameObs;
import client.Main;
import client.SupportiveThreads.LobbyRefreshThread;
import client.CurrentUser;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class LobbyScreen {

    @FXML
    Hyperlink logoutLink;

    @FXML
    Button createGame;

    @FXML
    Button spelSetup;

    @FXML
    Button joinButton;

    @FXML
    private Label joinErrorLabel;
    private RotateTransition rtAnimation;

    /* table en al zijn columns */
    @FXML
    public TableView<GameObs> activeGamesTable;

    @FXML
    public TableColumn<GameObs, String> gameIdColumn;

    @FXML
    public TableColumn<GameObs, String> clientAColumn;

    @FXML
    public TableColumn<GameObs, String> clientBColumn;

    @FXML
    public TableColumn<GameObs, String> fotoSetColumn;

    @FXML
    public TableColumn<GameObs, Integer> roosterSizeColumn;

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
            clientAColumn.setCellValueFactory(new PropertyValueFactory<>("clientA"));
            clientBColumn.setCellValueFactory(new PropertyValueFactory<>("clientB"));
            fotoSetColumn.setCellValueFactory(new PropertyValueFactory<>("fotoSet"));
            roosterSizeColumn.setCellValueFactory(new PropertyValueFactory<>("roosterSize"));

            // add data to table
            activeGamesTable.setItems(gamesObsList);

            // thread die om de 5 seconden de lobbytafel refresht aanmaken + opstarten
            Thread checkAvailableGames= new LobbyRefreshThread(this);
            checkAvailableGames.start();

            joinErrorLabel.setVisible(false);
            //shaketransition configureren
            rtAnimation = new RotateTransition(Duration.millis(50), joinErrorLabel);
            rtAnimation.setByAngle(10);
            rtAnimation.setCycleCount(8);
            rtAnimation.setAutoReverse(true);

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
        }
        else {
            //join this game
            int currentGameIdAttempt = deGameToJoin.getGameId();

            //try to join
            try {
                if(Main.cnts.getAppImpl().join(CurrentUser.getInstance().getUsername(), currentGameIdAttempt)){
                    CurrentGame.setInstance(Main.cnts.getAppImpl().getGame(currentGameIdAttempt));

                    // ga verder naar GAME
                    SpelViewLogica spv = new SpelViewLogica();
                    spv.start();

                    Platform.setImplicitExit(false);
                    spelSetup.getScene().getWindow().hide();
                }
                else{ // als geen successvolle join

                    joinErrorLabel.setVisible(true);
                    rtAnimation.play();
                    System.out.println("lobbyscreen.java: je bent een 3 e speler die probeert te joinen en dat mag niet!");

                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }




    }

}
