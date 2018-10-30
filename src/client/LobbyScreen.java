package client;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

import java.rmi.RemoteException;

public class LobbyScreen {

    @FXML
    Hyperlink logoutLink;

    @FXML
    public void initialize(){

        try {
            Main.cnts.getAppImpl().receiveHelloWorld("hello world");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
}
