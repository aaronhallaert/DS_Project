package client;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

public class LobbyScreen {

    @FXML
    Hyperlink logoutLink;


    public void logout(){
        Main.goToLogin();
        // Hide this current window
        logoutLink.getScene().getWindow().hide();
    }
}
