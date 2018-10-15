package client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;

import java.io.IOException;

public class LobbyScreen {

    @FXML
    Hyperlink logoutLink;


    public void logout(){
        Main.goToLogin();
        // Hide this current window
        logoutLink.getScene().getWindow().hide();
    }
}
