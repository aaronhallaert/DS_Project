package client.Controllers;

import Classes.GameInfo;
import client.Main;
import client.SupportiveThreads.WaitPlayerThread;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class SpelViewLogica extends Thread{

    private SpelViewGui spvGui;

    //lokale gameinfo
    private GameInfo gameInfo;


    @Override
    public void run() {

        try {
            //lokale gameinfo halen uit "echte" gameInfo
            gameInfo = Main.cnts.getAppImpl().getGameInfo(Main.currentGameId);

            // laden van screen enzo
            spvGui = loadAndSetGui();
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        // in deze thread wacht men op een 2de speler
        Thread waitPlayer= new WaitPlayerThread(this, gameInfo);
        waitPlayer.start();





    }

    /**
     * deze methode zorgt ervoor dat er niet geklikt kan worden op de kaarten als de 2de speler niet aanwezig is
     */
    public void changeInNumberOfPlayers(){
        if(!spvGui.isMouseClickEnabled()){
            spvGui.enableMouseClick();}

        else{
            spvGui.disableMouseClick();
        }

    }



    /** Laadt de gui, voert de initialize methode al uit. wacht hier op
     *
     * @return de instantie van de controller van de gui. deze is nodig om dan abstracte omdraaimethoden uit te voeren
     * @throws RemoteException
     */
    private SpelViewGui loadAndSetGui() throws RemoteException {
        Parent root = null;
        SpelViewGui controller =null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/spelViewGui.fxml"));
            root = fxmlLoader.load();
            controller = fxmlLoader.getController();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Parent finalRoot = root;
        Platform.runLater(
                () -> {
                    Stage spelViewStage= new Stage();
                    spelViewStage.setTitle("memory spel");
                    //width en height van de scene bepalen
                    //dit moet hier geset worden, jammergenoeg, we kunnen dit niet later aanpassen

                    ArrayList<Integer> values = null;
                    try {
                        values = bepaaldAfmetingenGameScreen();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    Scene startScene= new Scene(finalRoot,values.get(0) , values.get(1)); //misschien nog wijzigen
                    spelViewStage.setScene(startScene);
                    spelViewStage.setResizable(false);
                    spelViewStage.show();
                }
        );





        //setten van de controller variable

        return controller;
    }

    /**
     * screen size variabel afhankelijk van de grootte van het rooster
     * moet apart bepaald worden want we kunnen dat niet setten, read onli
     * @return
     * @throws RemoteException
     */
    private static ArrayList<Integer> bepaaldAfmetingenGameScreen() throws RemoteException {

        int width, height = 0;

        if(Main.cnts.getAppImpl().getGameInfo(Main.currentGameId).getRoosterSize() == 4){
            System.out.println("game van 4X4");
            width = 480;
            height = 520;
        }
        else{
            System.out.println("game van 6X6");
            width = 700;
            height = 750;

        }

        ArrayList<Integer> values = new ArrayList<Integer>();
        values.add(width);
        values.add(height);
        return values;

    }

    //hier komt methode die pollet naar commando's in de mailbox op de appserver
    //  van de specifieke game
    //  voor de specifieke speler
    // als er gevonden zijn :
    // list met commando's verwerken :              spvGui.executeCommandos(commandoList)


}
