package client;

import appserver.AppServerInterface;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class LoginScreen {

    @FXML
    Button loginButton;

    @FXML
    TextField usernameField;

    @FXML
    PasswordField passwordField;

    @FXML
    Hyperlink registreerLink;

    @FXML
    Label errorLoginMessage;

    Registry myRegistry;
    AppServerInterface appImpl;
    @FXML
    public void initialize(){
        try {
            //fire to localhost port 1900
            myRegistry = LocateRegistry.getRegistry("localhost", 1900);

            // search for application service
            appImpl = (AppServerInterface) myRegistry.lookup("AppserverService");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        errorLoginMessage.setVisible(false);
    }

    public void verwijsNaarRegistreerScreen(){
        Main.goToRegister();
        // Hide this current window (if this is what you want)
        registreerLink.getScene().getWindow().hide();
    }

    public void login(){

        String username=usernameField.getText();

        String password=passwordField.getText();
    try {
        if (appImpl.loginUser(username, password)) {
            System.out.println("login correct");
            errorLoginMessage.setVisible(false);
            Main.goToLobby();
            // Hide this current window (if this is what you want)
            registreerLink.getScene().getWindow().hide();
        } else {
            errorLoginMessage.setVisible(true);

        }
    }
    catch (Exception e){
        e.printStackTrace();
    }

    }
}
