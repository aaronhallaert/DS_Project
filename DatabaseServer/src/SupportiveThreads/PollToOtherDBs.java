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

        while (true) {

            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println();
            System.out.println();
            System.out.println("herbeginnen met de lus");

            for (int i = 0; i < otherPortNumbers.size(); i++) {

                try {


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

        }

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
