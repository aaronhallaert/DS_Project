package SupportiveThreads;

import interfaces.DatabaseInterface;

import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

/**
 * deze thread zorgt voor connectie met alle andere databases
 */
public class PollToOtherDBs extends Thread {

    /*----- info over andere databases -----------*/
    // poortnummers van de andere databases
    private ArrayList<Integer> otherPortNumbers;
    // interfaces naar andere databases
    private ArrayList<DatabaseInterface> dbImpls;
    // deze lijst houdt bij welke databases al reeds geconnecteerd zijn met deze database
    private ArrayList<Boolean> connected;

    public PollToOtherDBs(String dataBaseNaam) {

        otherPortNumbers = getOtherPortNumbers(dataBaseNaam);
        dbImpls = new ArrayList<DatabaseInterface>();
        connected = new ArrayList<>();
        // initieel zijn er nog geen connecties met andere databases
        for (int i = 0; i < 3; i++) {
            connected.add(false);
        }

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


            for (int i = 0; i < otherPortNumbers.size(); i++) {

                try {

                    // als nog niet connected
                    if (!connected.get(i)){
                        System.out.println("trying to connect to db"+otherPortNumbers.get(i));
                        DatabaseInterface dbImp = (DatabaseInterface) LocateRegistry.getRegistry("localhost", otherPortNumbers.get(i)).lookup("DatabaseService");
                        dbImpls.add(dbImp);
                    }

                    // connectie geslaagd
                    connected.set(i, true);

                } catch (Exception e) {
                    System.out.println("geen connectie met databaseServer op poort"+ otherPortNumbers.get(i)+" gevonden");
                }
            }


            setupDone = connectedToAll(connected);
            //setupDone true -> uit de loop

        }

        System.out.println("ik ben met alle andere databases verbonden");
        // vanaf hier moeten  we proberen zaken op te vangen

    }

    private boolean connectedToAll(ArrayList<Boolean> connected) {

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

    public ArrayList<DatabaseInterface> getDBRefs(){
        return dbImpls;
    }


}
