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
    public TableColumn<GameObs, String> hostColumn;

    @FXML
    public TableColumn<GameObs, String> fotoSetColumn;

    @FXML
    public TableColumn<GameObs, Integer> roosterSizeColumn;

    //todo: wss weg en direct callen
    //deze list moet in de gamesListViewer komen
    //todo: wrs dit automatisch refreshen na x aantal seconden? geen idee aparte thread ??
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
            //https://stackoverflow.com/questions/34794995/how-to-serialize-observablelist/34795127
            //https://medium.com/@keeptoo/adding-data-to-javafx-tableview-stepwise-df582acbae4f
            //deze strings zijn de exacte attribuutnamen van GameObs
            gameIdColumn.setCellValueFactory(new PropertyValueFactory<>("gameId"));
            hostColumn.setCellValueFactory(new PropertyValueFactory<>("host"));
            fotoSetColumn.setCellValueFactory(new PropertyValueFactory<>("fotoSet"));
            roosterSizeColumn.setCellValueFactory(new PropertyValueFactory<>("roosterSize"));

            // add data to table
            activeGamesTable.setItems(gamesObsList);

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

    //onclick van de create game knop
    public void createGame(){
        //todo: vul in?
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
                    SpelViewLogica spv = new SpelViewLogica();
                    spv.start();
                }
                else{ // als geen successvolle join

                    //todo: laat iets zien ivm foute / slechte join : er is al reeds een 2e speler

                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }




    }

}
