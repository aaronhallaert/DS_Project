package client;

import dataserver.DataServerMain;
import javafx.fxml.FXML;
import javafx.scene.control.*;


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

    DataServerMain dsm;

    @FXML
    public void initialize(){
        dsm=new DataServerMain();
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

        if(dsm.checkUser(username, password)){
            System.out.println("login correct");
            errorLoginMessage.setVisible(false);
            Main.goToLobby();
            // Hide this current window (if this is what you want)
            registreerLink.getScene().getWindow().hide();
        }
        else{
            errorLoginMessage.setVisible(true);
        }



    }
}
