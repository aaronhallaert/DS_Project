package client.Controllers;

import client.Connections;
import client.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class DisconnectionController {

    @FXML
    private Button goToLogin;

    public void goToLogin(){

        // als de dispatcher is uitgevallen probeer opnieuw connectie te maken
        Main.cnts= new Connections(1902, goToLogin.getScene());
        Main.goToLogin();
        goToLogin.getScene().getWindow().hide();
        Main.disconnected=false;
    }
}
