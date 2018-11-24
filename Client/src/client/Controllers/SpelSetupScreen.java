package client.Controllers;

import client.CurrentGame;
import client.Main;
import client.CurrentUser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.rmi.RemoteException;

public class SpelSetupScreen {

    @FXML
    RadioButton radioButton4X4;

    @FXML
    RadioButton radioButton6X6;

    @FXML
    RadioButton radioButtonSetA;

    @FXML
    RadioButton radioButtonSetB;

    @FXML
    RadioButton radioButtonSetC;

    @FXML
    Button startSpel;

    @FXML
    Button toLobby;

    //deze moet nog weg, is om te testen alsk een foto kan ophalen van de db
    @FXML
    Button test;

    @FXML
    ChoiceBox aantalSpelersPicker;

    @FXML
    ImageView fotoVoorbeeldSetA;

    @FXML
    ImageView fotoVoorbeeldSetB;

    @FXML
    ImageView fotoVoorbeeldSetC;

    @FXML
    Label errorMessage;

    //groepen zodat je maar 1 vd 3 radiobuttons kan selecteren
    ToggleGroup sizeGroup = new ToggleGroup();
    ToggleGroup fotoSetGroup = new ToggleGroup();



    @FXML
    public void initialize(){

        //buttons toevoegen aan de sets, zodat er maar 1 vd 3 tegelijk kan geselecteerd zijn
        radioButton4X4.setToggleGroup(sizeGroup);
        radioButton6X6.setToggleGroup(sizeGroup);

        radioButtonSetA.setToggleGroup(fotoSetGroup);
        radioButtonSetB.setToggleGroup(fotoSetGroup);
        radioButtonSetC.setToggleGroup(fotoSetGroup);

        //2buttons default al selected maken, bevordert door GUI vliegen
        radioButton4X4.setSelected(true);
        radioButtonSetA.setSelected(true);

        Main.setImage(fotoVoorbeeldSetA, "fotoSetA");
        Main.setImage(fotoVoorbeeldSetB, "fotoSetB");
        Main.setImage(fotoVoorbeeldSetC, "fotoSetC");

        aantalSpelersPicker.getItems().addAll(2, 3, 4);

        errorMessage.setVisible(false);
    }

    @FXML
    private void loadSettings() {

        int dimensies=0;
        char set= 'A';

        //bepalen hoe groot je het rooster wilt
        if(radioButton4X4.isSelected()){
            dimensies = 4;
        }
        else {
            dimensies = 6;
        }

        int aantalSpelers = 0;
        if(aantalSpelersPicker.getValue() != null){
            aantalSpelers = (int) aantalSpelersPicker.getValue();
            System.out.println("aantalSpelers is :"+aantalSpelers);
        }
        else{

            errorMessage.setVisible(true);
            System.out.println("geen aantalspelers gepicked");
            return;

        }

        //bepalen welke fotoset je wilt
        if(radioButtonSetA.isSelected()){
            set = 'A';
        }
        else if(radioButtonSetB.isSelected()){
            set = 'B';
        }
        else{set = 'C';}

        try{
            CurrentGame.setInstance(Main.cnts.getAppImpl().getGame(Main.cnts.getAppImpl().createGame(CurrentUser.getInstance().getUsername(), dimensies, set, aantalSpelers)));
            // opstarten spelview
            Main.goToSpel();
        }
        catch(RemoteException e){
            Main.fixDisconnection(radioButton4X4.getScene());
        }


        //dit is nodig zodat de UI thread niet stopt na het hiden van deze window
        Platform.setImplicitExit(false);
        // hide current scene
        radioButton4X4.getScene().getWindow().hide();
    }

    @FXML
    private void goToLobby(){

        //huidig scherm wegtoveren
        radioButton4X4.getScene().getWindow().hide();
        //naar lobby gaan
        Main.goToLobby();
    }


}
