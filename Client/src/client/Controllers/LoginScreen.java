package client.Controllers;


import client.Main;
import client.CurrentUser;
import interfaces.AppServerInterface;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


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

    @FXML
    Label sessionCancelled;

    @FXML
    Button imageUploadPage;

    @FXML
    public void initialize(){
        errorLoginMessage.setVisible(false);
        sessionCancelled.setVisible(false);
        //enter press trigers loginbutton
        loginButton.setDefaultButton(true);
    }

    public void verwijsNaarRegistreerScreen(){
        Main.goToRegister();
        // Hide this current window (if this is what you want)
        registreerLink.getScene().getWindow().hide();
    }

    public void loginWithToken(){
        try{
            boolean loggedin=Main.cnts.getAppImpl().loginWithToken(CurrentUser.getInstance().getToken(), CurrentUser.getInstance().getUsername());

            if(loggedin){
                sessionCancelled.setVisible(false);
                errorLoginMessage.setVisible(false);
                Main.goToLobby();
                registreerLink.getScene().getWindow().hide();
            }
            else{
                sessionCancelled.setVisible(true);
                errorLoginMessage.setVisible(false);
                System.out.println("uw sessie is vervallen error message");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void login(){

    String username=usernameField.getText();
    String password=passwordField.getText();

    try {
        if (Main.cnts.getAppImpl().loginUser(username, password)) {
            errorLoginMessage.setVisible(false);
            sessionCancelled.setVisible(false);
            CurrentUser.getInstance().setUsername(username);
            CurrentUser.getInstance().setToken(Main.cnts.getAppImpl().getToken(username));

            System.out.println(CurrentUser.getInstance().getUsername());
            System.out.println(Main.cnts.getAppImpl().getToken(username));
            Main.goToLobby();
            registreerLink.getScene().getWindow().hide();


            try {
                updateUserFile();
            }
            catch (IOException io){
                io.printStackTrace();
            }
        } else {
            sessionCancelled.setVisible(false);
            errorLoginMessage.setVisible(true);

            }
        }
        catch (Exception e){
            Main.fixDisconnection(usernameField.getScene());
        }

    }

    public void updateUserFile() throws IOException {
        FileWriter fileWriter = new FileWriter("Client/src/client/userfile.txt");
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(CurrentUser.getInstance().getUsername() +", " + CurrentUser.getInstance().getToken());
        printWriter.close();
    }

    @FXML
    public void goToUploadPage(){

        imageUploadPage.getScene().getWindow().hide();
        Main.gotoImageUploadPage();

    }

}
