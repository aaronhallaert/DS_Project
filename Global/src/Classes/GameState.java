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
    static int tegelsFlipped;
    static int vorigeTileUniqueId;
    static int huidigeTileUniqueId;
    static int aantalParenFound;
    // scores
    private int aantalParen;
    private int aantalPerRij;
    private String naamSpelerA;
    private String naamSpelerB;
    private List<Commando> inboxSpelerA;
    private List<Commando> inboxSpelerB;
    private int aantalPuntenSpelerA;
    private int aantalPuntenSpelerB;

    private ArrayList<Tile> tegelsList;
    //elke tegel moet bijgehouden worden, een tegel kan gewoon meerdere shit bevatten

    public GameState() {
        System.out.println("gameState default constructor opgeroepen, mag eig niet gebeuren");
        tegelsList = new ArrayList<Tile>();
        aantalPerRij = 0;
        aantalParen = 0;

    }

    public GameState(int gameId, int dimensions, char fotoSet, String hostName) {

        inboxSpelerA = new ArrayList<Commando>();
        inboxSpelerB = new ArrayList<Commando>();

        aantalParenFound =0;
        tegelsFlipped = 0;

        aantalPuntenSpelerA = 0;
        aantalPuntenSpelerB = 0;

        huidigeTileUniqueId = -1;
        vorigeTileUniqueId = -1;

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

    // als je active user hier zet, wordt het in de omgekeerde speler zijn spel uitgevoerd
    public synchronized void executeCommando(Commando commando, String activeUser) {

        //doorspelen van het commando naar de andere speler
        if (activeUser.equals(naamSpelerA)) {
            System.out.println("commando added in joinSpeler zijn inbox");
            inboxSpelerB.add(commando);
            notifyAll();
            System.out.println("executCommando notify in GameState");
        } else if (activeUser.equals(naamSpelerB)) {
            System.out.println("commando added in createSpeler zijn inbox");
            inboxSpelerA.add(commando);
            notifyAll();
            System.out.println("executCommando notify in GameState");
        } else {
            System.out.println("fout in executeCommando in GameState.java: geen commando added in mailbox andere pers");
        }

        vorigeTileUniqueId = huidigeTileUniqueId;
        huidigeTileUniqueId = commando.getUniqueTileId();

        tegelsFlipped++;

        //todo: deel dat de state hier nog aanpast ook nog setten. logica dat als 2 kaartjes geflipt zijn check ofzo
        pasStateAan(commando, activeUser);



    }

    private synchronized void executeCommandoBoth(Commando commando){

        inboxSpelerA.add(commando);
        inboxSpelerB.add(commando);
        notifyAll();

    }

    private void pasStateAan(Commando commando, String activeUser) {

        Tile huidigeTile = getTileMetUniqueId(tegelsList, commando.getUniqueTileId());

        huidigeTile.setFlippedOver(true);

        //zodat je niet 2x op dezelfde tile kan klikken om te vergelijken, anders zou het kloppen en krijg je punt
        executeCommandoBoth(new Commando("LOCK", huidigeTile.getUniqueIdentifier()));

        if(tegelsFlipped == 2){
            System.out.println("tegelsFlipped is 2 , hele boel logica nu");

            //logica van de 2 kaartjes die open liggen
            Tile vorigeTile = getTileMetUniqueId(tegelsList, vorigeTileUniqueId);

            if(huidigeTile.getId() == vorigeTile.getId()){


                System.out.println("tzijn dezelfde");
                System.out.println("award 1 punt aan de speler");



                //award punt
                awardPuntTo(activeUser);

                //pas state aan van de tegels
                huidigeTile.setFound(true);
                vorigeTile.setFound(true);

                //zorgen dat je er niet meer nog eens kan op drukken nadat ze gevonden zijn
                executeCommandoBoth(new Commando("LOCK", huidigeTile.getUniqueIdentifier()));
                executeCommandoBoth(new Commando("LOCK", vorigeTile.getUniqueIdentifier()));

                aantalParenFound++;

                //als alle tegels gevonden zijn
                if(aantalParenFound == aantalParen){

                    //who is the winner?
                    if(aantalPuntenSpelerA > aantalPuntenSpelerB){
                        // A is the winner
                        inboxSpelerA.add(new Commando("WIN",1));
                        inboxSpelerB.add(new Commando("LOSS",1));
                        notifyAll();

                    }
                    else if(aantalPuntenSpelerA < aantalPuntenSpelerB){
                        // B is the winner

                        inboxSpelerB.add(new Commando("WIN",1));
                        inboxSpelerA.add(new Commando("LOSS",1));
                        notifyAll();

                    }
                    else{
                        //its a draw
                        executeCommandoBoth(new Commando("DRAW",1));

                    }
                }



            }else{

                //pas state terug aan
                vorigeTile.setFlippedOver(false);
                huidigeTile.setFlippedOver(false);

                //draai ze terug om
                executeCommandoBoth(new Commando("UNFLIP", vorigeTile.getUniqueIdentifier()));
                executeCommandoBoth(new Commando("UNFLIP", huidigeTile.getUniqueIdentifier()));

                //zorgen dat deze tegels terug clickable zijn
                executeCommandoBoth(new Commando("UNLOCK", vorigeTile.getUniqueIdentifier()));
                executeCommandoBoth(new Commando("UNLOCK", huidigeTile.getUniqueIdentifier()));


                //misschien hier naar volgende speler gaan ofzo
                executeCommandoBoth(new Commando("SWITCH", 1)); // 1 is testwaarde, zodat we hem toch gaan vinden

            }
            //huidig kaartje is opvraagbaar in commando.getUniqueTileId();


            //finally

            //switch sides
            //andereSpelerAanDeBeurt

            //reset aantalTegelsTeler op 0
            tegelsFlipped = 0;
        }

    }

    private void awardPuntTo(String activeUser) {

        if(activeUser.equals(naamSpelerA)){
            aantalPuntenSpelerA ++;

                                        //deze 1 is een dummywaarde
            inboxSpelerA.add(new Commando("AWARDTOME", 1));
            inboxSpelerB.add(new Commando("AWARDTOYOU", 1));
        }

        else if(activeUser.equals(naamSpelerB)){
            aantalPuntenSpelerB ++;
            inboxSpelerB.add(new Commando("AWARDTOME", 1));
            inboxSpelerA.add(new Commando("AWARDTOYOU", 1));
        }

        else{
            System.out.println("fout in awardPuntTo : GameState");
        }

    }

    private Tile getTileMetUniqueId(ArrayList<Tile> tl, int uniqueTileId){

        for (Tile tile : tl) {
            if(tile.getUniqueIdentifier() == uniqueTileId){ return tile;}
        }

        System.out.println("fout in GameState.java : getTileMetUniqeId");
        return null;
    }

    public synchronized List<Commando> getInbox(String userName) {

        System.out.println("GameState: getInbox door user"+userName);
        if (userName.equals(naamSpelerA)) {

            while (inboxSpelerA.isEmpty()) {
                try {
                    System.out.println("inbox is leeg, wait started");
                    wait();
                    System.out.println("iets nieuws in de inbox van user " + naamSpelerA + ": wait stopped");
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            List<Commando> inbox = new ArrayList<Commando>(inboxSpelerA);
            inboxSpelerA.clear();
            System.out.println("inbox met grootte :"+inbox.size()+" gereturned");
            //miss moet de inbox nu leeggemaakt worden
            return inbox;

        } else if (userName.equals(naamSpelerB)) {

            while (inboxSpelerB.isEmpty()) {
                try {
                    System.out.println("inbox is leeg, wait started");
                    wait();
                    System.out.println("iets nieuws in de inbox van usser"+naamSpelerB+" : wait stopped");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            List<Commando> inbox = new ArrayList<Commando>(inboxSpelerB);
            inboxSpelerB.clear();
            System.out.println("inbox met grootte :"+inbox.size()+" gereturned");
            //miss moet de inbox nu leeggemaakt worden
            return inbox;

        }
        System.out.println("fout in getInbox in GameState.java");
        return null;
    }

}