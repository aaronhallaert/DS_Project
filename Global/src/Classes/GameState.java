package Classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
    hierin komt alles dat de gameState kan bepalen
    NIET DENKEN AAN VISUALISATIE, WEL AAN WAT NODIG IS OM GUI TE VORMEN -> light weight
 */
public class GameState implements Serializable {

    // wie aan de beurt
    // scores
    private int aantalParen;
    private int aantalPerRij;
    private String naamSpelerA;
    private String naamSpelerB;
    private List<Commando> inboxSpelerA;
    private List<Commando> inboxSpelerB;

    private ArrayList<Tile> tegelsList;//elke tegel moet bijgehouden worden, een tegel kan gewoon meerdere shit bevatten

    public GameState() {
        tegelsList = new ArrayList<Tile>();
        aantalPerRij = 0;
        aantalParen = 0;
    }

    public GameState(int gameId, int dimensions, char fotoSet, String hostName) {

        inboxSpelerA = new ArrayList<Commando>();
        inboxSpelerB = new ArrayList<Commando>();

        naamSpelerA = hostName;
        aantalParen = dimensions * dimensions / 2;
        aantalPerRij = dimensions;

        int offset = 0;
        if (fotoSet == 'B') {
            offset = 100;
        } else if (fotoSet == 'C') {
            offset = 300;
        }

        tegelsList = new ArrayList<Tile>();

        int uniqueId = 1;

        for (int i = 0; i < aantalParen; i++) {

            tegelsList.add(new Tile(uniqueId, i, i + offset + 1 + "", "0"));
            uniqueId++;
            tegelsList.add(new Tile(uniqueId, i, i + offset + 1 + "", "0"));
            uniqueId++;
        }

        Collections.shuffle(tegelsList);


    }

    public void join(String secondUserName) {
        naamSpelerB = secondUserName;
        System.out.println("join in gameState succesvol");
    }

    public int getAantalParen() {
        return aantalParen;
    }

    public int getAantalPerRij() {
        return aantalPerRij;
    }

    public ArrayList<Tile> getTegelsList() {
        return tegelsList;
    }


    public //synchronized
    void executeCommando(Commando commando, String activeUser) {

        if (activeUser.equals(naamSpelerA)) {
            System.out.println("commando added in joinSpeler zijn inbox");
            inboxSpelerB.add(commando);
            //notifyAll();
            System.out.println("executCommando notify in GameState");
        } else if (activeUser.equals(naamSpelerB)) {
            System.out.println("commando added in createSpeler zijn inbox");
            inboxSpelerA.add(commando);
            //notifyAll();
            System.out.println("executCommando notify in GameState");
        } else {
            System.out.println("fout in executeCommando in GameState.java: geen commando added in mailbox andere pers");
        }

        //todo: deel dat de state hier nog aanpast ook nog setten. logica dat als 2 kaartjes geflipt zijn check ofzo

    }

    public
    //synchronized
    List<Commando> getInbox(String userName) {

        System.out.println("GameState: getInbox door user"+userName);
        if (userName.equals(naamSpelerA)) {

            while (inboxSpelerA.isEmpty()) {
                //try {
                    System.out.println("inbox is leeg, wait started");
                    //wait();
                    System.out.println("iets nieuws in de inbox van user "+naamSpelerA+": wait stopped");
                /*} catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }

            List<Commando> inbox = new ArrayList<Commando>(inboxSpelerA);
            System.out.println("inbox met grootte :"+inbox.size()+" gereturned");
            //miss moet de inbox nu leeggemaakt worden
            return inbox;

        } else if (userName.equals(naamSpelerB)) {

            while (inboxSpelerB.isEmpty()) {
                //try {
                    System.out.println("inbox is leeg, wait started");
                    //wait();
                    System.out.println("iets nieuws in de inbox van usser"+naamSpelerB+" : wait stopped");
                /*} catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }

            List<Commando> inbox = new ArrayList<Commando>(inboxSpelerB);
            System.out.println("inbox met grootte :"+inbox.size()+" gereturned");
            //miss moet de inbox nu leeggemaakt worden
            return inbox;

        }
        System.out.println("fout in getInbox in GameState.java");
        return null;
    }

}