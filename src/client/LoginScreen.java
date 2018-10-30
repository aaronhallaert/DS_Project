package client;


import appserver.AppServiceImpl;
import interfaces.AppServerInterface;
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


    @FXML
    public void initialize(){
        errorLoginMessage.setVisible(false);
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
                errorLoginMessage.setVisible(false);
                Main.cnts.setAppImpl(appImpl);
                Main.goToLobby();
                registreerLink.getScene().getWindow().hide();
            }
            else{
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
            Main.cnts.setAppImpl(appImpl);
            User.getCurrentUser().setUsername(username);
            User.getCurrentUser().setToken(Main.cnts.getDispatchImpl().getToken(username));

            System.out.println(User.getCurrentUser().getUsername());
            System.out.println(Main.cnts.getDispatchImpl().getToken(username));
            Main.goToLobby();
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
