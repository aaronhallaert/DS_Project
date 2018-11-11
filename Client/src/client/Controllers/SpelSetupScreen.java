package client.Controllers;

import client.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
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
    ImageView fotoVoorbeeldSetA;

    @FXML
    ImageView fotoVoorbeeldSetB;

    @FXML
    ImageView fotoVoorbeeldSetC;

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
    }

    @FXML
    private void loadSettings() throws RemoteException {

        int dimensies=0;
        char set= 'A';

        //bepalen hoe groot je het rooster wilt
        if(radioButton4X4.isSelected()){
            dimensies = 4;
        }
        else {
            dimensies = 6;
        }


        //bepalen welke fotoset je wilt
        if(radioButtonSetA.isSelected()){
            set = 'A';
        }
        else if(radioButtonSetB.isSelected()){
            set = 'B';
        }
        else{set = 'C';}

        //todo: insert logica om een state te maken, appserver shit daare
        Main.currentGameId = Main.cnts.getAppImpl().createGame(Main.activeUser, dimensies, set);
        Main.cnts.getAppImpl().getGameInfo(Main.currentGameId).join(Main.activeUser);
        //fire up spelScreen, empty?
        Main.goToSpel();


        //switchen naar een bepaalde key ipv Main.activeUser
        //Main.cnts.getAppImpl().getGameData(Main.activeUser);


        //Main.goToSpel(dimensies, set);



    }

    @FXML
    private void goToLobby(){

        //huidig scherm wegtoveren
        radioButton4X4.getScene().getWindow().hide();
        //naar lobby gaan
        Main.goToLobby();
    }


}
