package client.Controllers;

import client.Main;
import client.Views.ScoreObs;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;



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

    public static ObservableList<ScoreObs> scoreObsLijst;

    @FXML
    public void initialize(){





    }

    @FXML
    public void goToLobby(){
        Main.goToLobby();
    }
}
