package client;

import dataserver.DataServerMain;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.xml.crypto.Data;
import java.util.Scanner;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

        //inloggen of registreren
        Scanner sc= new Scanner(System.in);
        System.out.println("inloggen of registreren?");
        DataServerMain dsm= new DataServerMain();
        String username="";
        String password="";

        switch (sc.nextLine()){
            case "inloggen":
                System.out.println("je koos voor inloggen");
                System.out.println("username");
                username=sc.nextLine();
                System.out.println("password");
                password=sc.nextLine();


                if(dsm.checkUser(username, password)){
                    System.out.println("login correct");
                }
                else{
                    System.out.println("login incorrect");
                }




                break;
            case "registreren":
                System.out.println("je kooos voor registreren");
                System.out.println("username");
                username=sc.nextLine();
                System.out.println("password");
                String first=sc.nextLine();
                System.out.println("bevestig password");
                while(!first.equals(password)) {
                    if (first.equals(sc.nextLine())) {
                        password = first;
                        // save user and password in database dit MOET VIA APPSERVER

                        dsm.insertUser(username, password);
                    }
                    else{
                        System.out.println("foutieve bevestiging, probeer opnieuw");
                    }
                }
                break;
            default:
                System.out.println("invalid input");
                break;
        }


    }


    public static void main(String[] args) {
        launch(args);
    }
}
