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
            AppServerInterface appImpl=Main.cnts.getDispatchImpl().loginWithToken(CurrentUser.getInstance().getToken(), CurrentUser.getInstance().getUsername());

            if(appImpl != null){
                sessionCancelled.setVisible(false);
                errorLoginMessage.setVisible(false);
                Main.cnts.setAppImpl(appImpl);
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
        AppServerInterface appImpl;
        if ((appImpl=Main.cnts.getDispatchImpl().loginUser(username, password))!=null) {
            errorLoginMessage.setVisible(false);
            sessionCancelled.setVisible(false);
            Main.cnts.setAppImpl(appImpl);
            CurrentUser.getInstance().setUsername(username);
            CurrentUser.getInstance().setToken(Main.cnts.getDispatchImpl().getToken(username));

            System.out.println(CurrentUser.getInstance().getUsername());
            System.out.println(Main.cnts.getDispatchImpl().getToken(username));
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
