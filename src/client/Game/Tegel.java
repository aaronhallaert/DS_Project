package client.Game;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/*
    tegel die we kunnen aanklikken
    hier komen dan later nog afbeeldingen is ofzo t een of  t ander

 */
public class Tegel extends StackPane {

    private Text text =new Text();


    public Tegel(String waarde){
        //hier: letters in de tiles, wij willen afbeeldingen

        Rectangle border = new Rectangle(50,50); //size
        border.setFill(null); //verwijdert de kleur van de tile
        border.setStroke(Color.BLACK);

        text.setText(waarde);
        text.setFont(Font.font(30));

        setAlignment(Pos.CENTER); //alls int midden van de tile

        getChildren().addAll(border, text);

        //logica bij klikken op muis
        setOnMouseClicked(this::handleMouseClick);

        //by default is de tegel gesloten
        close();

    }

    //als je der op klikt?
    public void open(Runnable action){ //action die geexecuted wordt wanneer de tile volledig geopend is
        FadeTransition ft = new FadeTransition(Duration.seconds(0.5), text);
        ft.setToValue(1); //wat de animatie zal afsluiten ofzo?
        ft.setOnFinished(e -> action.run()); //???
        ft.play(); //animatie executen
    }

    //als je der nog keer op klikt?
    public void close(){
        FadeTransition ft = new FadeTransition(Duration.seconds(0.5), text);
        ft.setToValue(0); //wat de animatie zal afsluiten ofzo?
        ft.play(); //animatie executen
    }

    public boolean isOpen(){
        return text.getOpacity() == 1;
    }

    public boolean hasSameValue(Tegel andereTegel){
        return this.text.getText().equals(andereTegel.text.getText());
    }

    public void handleMouseClick(MouseEvent event){
        if( isOpen() || MemoryPuzzleApp.clickCount == 0){return;} //als je op een tile klikt die al open is, nieks gebeurd

        MemoryPuzzleApp.clickCount--; //semafoor stuff

        if( MemoryPuzzleApp.selectedTile == null){ //als er nog een van de 2 geopend is

            MemoryPuzzleApp.selectedTile = this;
            open(() ->{});
        }
        else{ //als der dus al een tile selected is
            //vergelijken
            open(() -> {

                if(!hasSameValue(MemoryPuzzleApp.selectedTile)){ //als ze verschillende waarde hebben
                    MemoryPuzzleApp.selectedTile.close();
                    this.close();
                }

                //resetten
                MemoryPuzzleApp.selectedTile = null;
                MemoryPuzzleApp.clickCount = 2;

            });

        }




    }

}
