package client.Controllers;

import Classes.GameInfo;
import client.GameObs;
import client.Main;
import client.SupportiveThreads.LobbyRefreshThread;
import client.User;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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
            Main.cnts.getAppImpl().receiveHelloWorld("hello world");

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

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void refresh(){
        activeGamesTable.setItems(gamesObsList);
    }

    public void logout(){
        try {
            Main.cnts.getDispatchImpl().logoutUser(User.getCurrentUser().getUsername());
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
        }
        else {
            //join this game
            int currentGameIdAttempt = deGameToJoin.getGameId();

            //try to join
            try {
                if(Main.cnts.getAppImpl().join(Main.activeUser, currentGameIdAttempt)){
                    Main.currentGameId=currentGameIdAttempt;
//                    spelSetup.getScene().getWindow().hide();
                    SpelViewLogica spv = new SpelViewLogica();
                    spv.start();
                }
                else{ // als geen successvolle join
                    System.out.println("joinen voor een 2e keer!!!!! implementeer dit a broer");
                    System.out.println("Lobbyscreen.java : join methode");

                    if(Main.cnts.getAppImpl().rejoin(Main.activeUser, currentGameIdAttempt)){

                        SpelViewLogica spv = new SpelViewLogica();
                        spv.start();

                    }
                    else{
                        System.out.println("lobbyscreen.java: je bent een 3 e speler die probeert te joinen en dat mag niet!");
                        //todo: laat iets zien ivm foute / slechte join : er is al reeds een 2e speler
                    }


                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }




    }

}
