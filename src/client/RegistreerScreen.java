package client;

import dataserver.DataServerMain;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegistreerScreen {

    @FXML
    Button registreerButton;

    @FXML
    Hyperlink loginLink;

    @FXML
    TextField usernameField;

    @FXML
    PasswordField passwordField;

    @FXML
    PasswordField confirmPasswordField;

    @FXML
    Label errorMessagePassword;

    @FXML
    Label errorMessageUsername;

    DataServerMain dsm;

    @FXML
    public void initialize(){
        dsm=new DataServerMain();
        errorMessagePassword.setVisible(false);
        errorMessageUsername.setVisible(false);
    }

    public void registreer(){

        errorMessagePassword.setVisible(false);
        errorMessageUsername.setVisible(false);
        String username=usernameField.getText();

        String firstPassword= passwordField.getText();
        String confirmPassword= confirmPasswordField.getText();

        if(!dsm.userNameExists(username)) {

            if (firstPassword.equals(confirmPassword)) {


                // save user and password in database dit MOET VIA APPSERVER
                dsm.insertUser(username, confirmPassword);


                verwijsNaarLoginScreen();
            } else {
                errorMessagePassword.setVisible(true);
            }

        }
        else{
            errorMessageUsername.setVisible(true);
        }


    }

    public void verwijsNaarLoginScreen(){
        Main.goToLogin();
        // Hide this current window (if this is what you want)
        loginLink.getScene().getWindow().hide();
    }

}
