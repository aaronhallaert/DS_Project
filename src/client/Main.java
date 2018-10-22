package client;

import appserver.AppServerInterface;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main extends Application {

    public static Registry appRegistry;
    public static AppServerInterface impl;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("loginscreen.fxml"));
        primaryStage.setTitle("Memory Login");
        Scene startScene= new Scene(root,386 , 323);
        primaryStage.setScene(startScene);
        primaryStage.setResizable(false);
        primaryStage.show();


        // setup communicatie met application server
        // fire to localhost port 1900
        appRegistry= LocateRegistry.getRegistry("localhost",1900);
        // search for application service
        impl=(AppServerInterface) appRegistry.lookup("AppserverService");
        impl.receiveHelloWorld("hello world");

    }


    public static void goToLogin(){
        // back to login
        Parent root = null;
        try {
            root = FXMLLoader.load(Main.class.getResource("loginScreen.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage registrerStage= new Stage();
        registrerStage.setTitle("Memory Login");
        Scene startScene= new Scene(root,386 , 323);
        registrerStage.setScene(startScene);
        registrerStage.setResizable(false);
        registrerStage.show();

    }

    public static void goToRegister(){
        Parent root = null;
        try {
            root = FXMLLoader.load(Main.class.getResource("registreerScreen.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage registrerStage= new Stage();
        registrerStage.setTitle("Memory Register");
        Scene startScene= new Scene(root,386 , 423);
        registrerStage.setScene(startScene);
        registrerStage.setResizable(false);
        registrerStage.show();
    }

    public static void goToLobby(){
        Parent root = null;
        try {
            root = FXMLLoader.load(Main.class.getResource("lobbyScreen.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage registrerStage= new Stage();
        registrerStage.setTitle("Memory Lobby");
        Scene startScene= new Scene(root,600 , 465);
        registrerStage.setScene(startScene);
        registrerStage.setResizable(false);
        registrerStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
