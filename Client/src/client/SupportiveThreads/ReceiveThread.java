package client.SupportiveThreads;

import Classes.Commando;
import client.Controllers.SpelViewGui;
import client.Main;
import javafx.application.Platform;

import java.rmi.RemoteException;

public class ReceiveThread extends Thread {

    private String userName;
    private int gameId;
    private SpelViewGui spv;

    public ReceiveThread(String userName, int gameId, SpelViewGui spv){
        this.userName = userName;
        this.gameId =gameId;
    }


    @Override
    public void run() {
        super.run();

        System.out.println("receivethread started");
        System.out.println("met naam "+userName);

        while(true){
            try{
                for( Commando c : Main.cnts.getAppImpl().getInbox(userName,gameId)){
                    System.out.println("commando gevonden, proberen te executen");
                    Platform.runLater(()->{
                        spv.executeCommando(c);
                    });
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
