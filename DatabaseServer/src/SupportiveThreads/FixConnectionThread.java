package SupportiveThreads;

import interfaces.DatabaseInterface;

import javax.sound.midi.Soundbank;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class FixConnectionThread extends Thread {

    private int poortNummer;
    private DatabaseInterface dbInterface;
    private boolean reconnected;

    public FixConnectionThread(Integer portNumber, DatabaseInterface databaseInterface) {
        this.poortNummer = portNumber;
        this.dbInterface = databaseInterface;
        reconnected = false;
    }

    @Override
    public synchronized void start() {
        super.start();

        while(!reconnected){

            System.out.println("trying to connect to db"+poortNummer);
            try {
                DatabaseInterface dbImp = (DatabaseInterface) LocateRegistry.getRegistry("localhost", poortNummer).lookup("DatabaseService");

                //indien niet gesprongen nar exception
                reconnected = true;

            } catch (RemoteException e) {
                System.out.println("server not found");
            } catch (NotBoundException e) {
                System.out.println("service not found");
            }


            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }



    }

    public boolean getReconnected(){
        return reconnected;
    }


    /*
            try {

            // als nog niet connected
            if (!connected.get(i)){
                System.out.println("trying to connect to db"+otherPortNumbers.get(i));
                DatabaseInterface dbImp = (DatabaseInterface) LocateRegistry.getRegistry("localhost", otherPortNumbers.get(i)).lookup("DatabaseService");
                dbImpls.add(dbImp);
            }

            System.out.println("connected to "+otherPortNumbers.get(i));
            connected.set(i, true);


        } catch (Exception e) {
            System.out.println("geen connectie met databaseServer op poort " + otherPortNumbers.get(i) + "gevonden");
        }
     */
}
