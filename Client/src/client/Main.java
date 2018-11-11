package client;

import Classes.GameInfo;
import client.Controllers.SpelViewLogica;
import client.Game.MemoryPuzzleApp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;


public class Main extends Application {

    public static String activeUser;
    public static Connections cnts;
    public static int currentGameId = 0;

    // ENKEL VOOR STATISCHE FOTOS, NIET VOOR MEMORYSPEL ZELF DUS
    //om een foto in te laden van de database, en hem direct te storen in een ImageView
    public static void setImage(ImageView kader, String naam) {

        byte[] afbeelding = null;

        try {

            afbeelding = Main.cnts.dispatchImpl.getImage(naam);

            //converteren van de byte[] naar een waarde die imageView aankan
            ByteArrayInputStream is =  new ByteArrayInputStream(afbeelding);
            Image image = new Image(is);

            //deze waarde in de ImageView zetten
            kader.setImage(image);


        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //TODO: kijken als dit misschien niet Image moet returnen
    public static byte[] loadImageBytes(String naam){

        byte[] afbeelding = null;

        try {

            afbeelding = Main.cnts.dispatchImpl.getImage(naam);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return afbeelding;
    }

    public static ObservableList<GameObs> configureList(ArrayList<GameInfo> gameInfoLijst) {

        ArrayList<GameObs> returnList = new ArrayList<GameObs>();

        for (GameInfo gameInfo : gameInfoLijst) {
            returnList.add(new GameObs(gameInfo));
        }

        return FXCollections.observableArrayList(returnList);

    }


    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("Views/loginScreen.fxml"));
        primaryStage.setTitle("Memory Login");
        Scene startScene= new Scene(root,386 , 323);
        primaryStage.setScene(startScene);
        primaryStage.setResizable(false);
        primaryStage.show();

        cnts= new Connections(1902);


        // @TODO load in username en token from txt file
        BufferedReader br = new BufferedReader(new FileReader("Client/src/client/userfile.txt"));
        String line= br.readLine();
        br.close();
        String[] gegevens= line.split(", ");
        User.getCurrentUser().setUsername(gegevens[0]);
        User.getCurrentUser().setToken(gegevens[1]);
    }





    // ALLE GO TO METHODEN KOMEN HIER
    // ALLE GO TO METHODEN KOMEN HIER
    // ALLE GO TO METHODEN KOMEN HIER
    // ALLE GO TO METHODEN KOMEN HIER

    public static void goToLogin(){
        // back to login
        Parent root = null;
        try {
            root = FXMLLoader.load(Main.class.getResource("Views/loginScreen.fxml"));
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
            root = FXMLLoader.load(Main.class.getResource("Views/registreerScreen.fxml"));
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
            root = FXMLLoader.load(Main.class.getResource("Views/lobbyScreen.fxml"));
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

    //todo: remove goToSpelMethodes!
    //TODO: spel starten met bepaalde configuraties gebeurd vanuit hier
    //todo: verouderde methode...
    public static void goToSpel(int dimensies, char set) {

        Parent root = null;
        System.out.println("passé met parameters");

        // wrapper met Platform klasse want je kan geen klasse die erft van 'Application' runnen in
        // een already running application
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                new MemoryPuzzleApp().start(new Stage(), dimensies, set);
            }
        });


    }

    // deze methode wordt nu gebruikt om het spel in te laden met de current id
    public static void goToSpel() throws RemoteException {

        //spelviewlogica is een thread, deze zal onder meer de gui thread aansturen
        SpelViewLogica spv = new SpelViewLogica();
        spv.start();


    }

    public static void goToSetupSpel() {

        Parent root = null;
        try {
            root = FXMLLoader.load(Main.class.getResource("Views/spelSetup.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage spelSetupStage= new Stage();
        spelSetupStage.setTitle("setup");
        Scene startScene= new Scene(root,600 , 465); //misschien nog wijzigen
        spelSetupStage.setScene(startScene);
        spelSetupStage.setResizable(false);
        spelSetupStage.show();

    }

    public static void gotoImageUploadPage() {

        Parent root = null;
        try {
            root = FXMLLoader.load(Main.class.getResource("Views/imageUploader.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage imageUploadStage= new Stage();
        imageUploadStage.setTitle("upload image");
        Scene startScene= new Scene(root,600 , 465);
        imageUploadStage.setScene(startScene);
        imageUploadStage.setResizable(false);
        imageUploadStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}