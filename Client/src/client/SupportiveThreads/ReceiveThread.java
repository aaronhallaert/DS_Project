package client.SupportiveThreads;

import Classes.Commando;
import client.Controllers.SpelViewGui;
import client.Main;
import javafx.application.Platform;

import java.rmi.RemoteException;

/**
 * deze thread pullt de inbox van de speler naar de client, alleen maar als er een item in de inbox is toegevoegd
 * de thread geeft dan het commando door aan de Gui om te zien wat er moet aangepast worden lokaal in de gui
 */
public class ReceiveThread extends Thread {

    private String userName;
    private int gameId;
    private SpelViewGui spv;

    public ReceiveThread(String userName, int gameId, SpelViewGui spv){
        this.userName = userName;
        this.gameId =gameId;
        this.spv = spv;
    }


    @Override
    public void run() {
        super.run();

        System.out.println("receivethread started");
        System.out.println("met naam "+userName);
        try{
        while(true){

                for(Commando c : Main.cnts.getAppImpl().getInbox(userName,gameId)){
                    //System.out.println("commando gevonden, proberen te executen");
                    Platform.runLater(()->{
                        try {
                            spv.executeCommando(c);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    });
                }
        }
        } catch (RemoteException e) {

            System.out.println("application server is uitgevallen");

        }
    }
}
