package client.Controllers;

import client.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageUploaderController {

    @FXML
    Button uploadButton;

    @FXML
    TextField naam;

    @FXML
    Label uploaded;

    final FileChooser fileChooser = new FileChooser();

    @FXML
    public void upload() throws IOException {
        File file = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        if(file != null){
            //store File
            System.out.println("gelukt");

            //converteren van de file naar een blob
            byte[] afbeelding = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(afbeelding);
            fis.close();

            //nu nog de byte array versturen.
            String naamFoto = naam.getText().toString();

            Main.cnts.dispatchImpl.storeImage(naamFoto, afbeelding);

            uploaded.setVisible(true);
        }
        else{
            System.out.println("file is null");
            uploaded.setVisible(false);
        }
    }

    @FXML
    public void initialize(){
        uploaded.setVisible(false);
    }


}
