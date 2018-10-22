package client;

import appserver.AppServerInterface;
import dataserver.DataServerMain;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

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
        errorMessagePassword.setVisible(false);
        errorMessageUsername.setVisible(false);
    }

    public void registreer() throws RemoteException {

        errorMessagePassword.setVisible(false);
        errorMessageUsername.setVisible(false);
        String username=usernameField.getText();

        String firstPassword= passwordField.getText();
        String confirmPassword= confirmPasswordField.getText();

        if(!appImpl.userNameExists(username)) {

            if (firstPassword.equals(confirmPassword)) {



                appImpl.insertUser(username, confirmPassword);


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
