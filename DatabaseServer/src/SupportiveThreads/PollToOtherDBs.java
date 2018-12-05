package SupportiveThreads;

import interfaces.DatabaseInterface;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

public class PollToOtherDBs extends Thread {

    private ArrayList<Integer> otherPortNumbers;

    private ArrayList<DatabaseInterface> dbImpls;
    private ArrayList<Boolean> connected;

    public PollToOtherDBs(String dataBaseNaam) {

        otherPortNumbers = getOtherPortNumbers(dataBaseNaam);
        dbImpls = new ArrayList<DatabaseInterface>();
        connected = new ArrayList<>();

        //int begin falses stoppen in die shizzl lijst ma nizzl
        for (int i = 0; i < 3; i++) {
            connected.add(false);
        }

        System.out.println("poll to other dbs thread constructor triggerd");

    }

    @Override
    public void run() {

        //todo: wat als er een uitvalt?

        super.run();

        boolean setupDone = false;


        //we springen uit deze while loop eenmaal alle databases connecteerd zijn met elkaar
        while (!setupDone) {

            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println();
            System.out.println("herbeginnen met de lus");

            for (int i = 0; i < otherPortNumbers.size(); i++) {

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
            }


            setupDone = zijnWeKlaar(connected);
            //setupDone true -> uit de loop

        }

        System.out.println("alle db's verbonden, we  gaan  over tot volgende while");
        // vanaf hier moeten  we proberen zaken op te vangen

        //todo: voorlopig niet kijken voor deze zaken, eerst zorgen dat een write gerepliqueerd wordt
        /*while(true){

            for(int i=0 ; i<dbImpls.size(); i++){

                if(connected.get(i)){
                    try {
                        if(dbImpls.get(i).ping()){
                            System.out.println("nog steeds connectie naar "+otherPortNumbers.get(i));
                        }
                    } catch (RemoteException e) { //if connection broke

                        connected.set(i, false);
                        System.out.println("connection lost to"+otherPortNumbers.get(i));
                        //start thread op die de connectie restart?
                        FixConnectionThread fct = new FixConnectionThread(otherPortNumbers.get(i), dbImpls.get(i));
                        fct.start();
                    }

                }

            }

            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }*/


    }

    private boolean zijnWeKlaar(ArrayList<Boolean> connected) {

        boolean klaar = true;

        for (Boolean aBoolean : connected) {

            if(!aBoolean){
                klaar  = false;
            }

        }

        return klaar;

    }


    private ArrayList<Integer> getOtherPortNumbers(String databaseNaam) {

        ArrayList<Integer> poortNummers = new ArrayList<>();
        poortNummers.add(1940);
        poortNummers.add(1950);
        poortNummers.add(1960);
        poortNummers.add(1970);

        Integer a = Integer.parseInt(databaseNaam);
        poortNummers.remove(a);

        return poortNummers;
    }


}
