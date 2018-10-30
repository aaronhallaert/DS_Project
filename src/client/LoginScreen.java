package client;


import appserver.AppServiceImpl;
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
    public void initialize(){
        errorLoginMessage.setVisible(false);
        sessionCancelled.setVisible(false);
    }

    public void verwijsNaarRegistreerScreen(){
        Main.goToRegister();
        // Hide this current window (if this is what you want)
        registreerLink.getScene().getWindow().hide();
    }

    public void loginWithToken(){
        try{
            AppServerInterface appImpl=Main.cnts.getDispatchImpl().loginWithToken(User.getCurrentUser().getToken(), User.getCurrentUser().getUsername());

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
            User.getCurrentUser().setUsername(username);
            User.getCurrentUser().setToken(Main.cnts.getDispatchImpl().getToken(username));

            System.out.println(User.getCurrentUser().getUsername());
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
        e.printStackTrace();
    }

    }

    public void updateUserFile() throws IOException {
        FileWriter fileWriter = new FileWriter("src/client/userfile.txt");
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(User.getCurrentUser().getUsername() +", " + User.getCurrentUser().getToken());
        printWriter.close();
    }
}
