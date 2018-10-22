package client;

import interfaces.AppServerInterface;
import interfaces.DispatchInterface;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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


    @FXML
    public void initialize(){

        errorMessagePassword.setVisible(false);
        errorMessageUsername.setVisible(false);
    }

    public void registreer() throws RemoteException {

        errorMessagePassword.setVisible(false);
        errorMessageUsername.setVisible(false);
        String username=usernameField.getText();

        String firstPassword= passwordField.getText();
        String confirmPassword= confirmPasswordField.getText();

        if(!Main.cnts.getDispatchImpl().userNameExists(username)) {

            if (firstPassword.equals(confirmPassword)) {



                Main.cnts.getDispatchImpl().insertUser(username, confirmPassword);


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
