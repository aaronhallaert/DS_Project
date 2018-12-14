package client.Controllers;

import Classes.Score;
import client.Main;
import client.ScoreObs;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.rmi.RemoteException;
import java.util.ArrayList;


public class ScoreScreen {

    @FXML
    private Button naarLobbyButton;

    @FXML
    public TableView<ScoreObs> scoresTable;

    @FXML
    public TableColumn<ScoreObs, String> nameColumn;

    @FXML
    public TableColumn<ScoreObs, Integer> aantalGamesColumn;

    @FXML
    public TableColumn<ScoreObs, Integer> aantalWinsColumn;

    @FXML
    public TableColumn<ScoreObs, Integer> aantalLossesColumn;

    @FXML
    public TableColumn<ScoreObs, Integer> aantalDrawsColumn;

    @FXML
    public TableColumn<ScoreObs, Integer> max4X4Column;

    @FXML
    public TableColumn<ScoreObs, Integer> max6X6Column;

    public static ArrayList<Score> scoreLijst = new ArrayList<Score>();
    public static ObservableList<ScoreObs> scoreObsLijst;

    @FXML
    public void initialize(){

        refreshScores();
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("naamSpeler"));
        aantalGamesColumn.setCellValueFactory(new PropertyValueFactory<>("aantalGames"));
        aantalWinsColumn.setCellValueFactory(new PropertyValueFactory<>("aantalWins"));
        aantalLossesColumn.setCellValueFactory(new PropertyValueFactory<>("aantalLosses"));
        aantalDrawsColumn.setCellValueFactory(new PropertyValueFactory<>("aantalDraws"));
        max4X4Column.setCellValueFactory(new PropertyValueFactory<>("aantal4X4"));
        max6X6Column.setCellValueFactory(new PropertyValueFactory<>("aantal6X6"));

        // add data to the table
        scoresTable.setItems(scoreObsLijst);





    }

    private void refreshScores() {

        scoreLijst.clear();
        ArrayList<ScoreObs> scoreObservableList = new ArrayList<>();

        try {

            ArrayList<Score> toConvert = Main.cnts.getAppImpl().getScores();

            System.out.println("wat zit er in scoreList?");
            for (Score score : toConvert) {

                scoreObservableList.add(new ScoreObs(score));

            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("wat zit er in scoreObs");

        scoreObsLijst = FXCollections.observableArrayList(scoreObservableList);


    }

    @FXML
    public void goToLobby(){

        Platform.setImplicitExit(false);
        naarLobbyButton.getScene().getWindow().hide();

        Main.goToLobby();
    }
}
