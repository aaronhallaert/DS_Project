package Classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*  is altijd een deel van een Game
    hier in staat voldoende info om een volledige game her in te laden
    hier gebeurt ook de logica van het spelletje, wanneer kaartjes worden opgengehouden,
    de informatie wordt hier ook (door)gegeven van/ aan speler 1 <-> speler 2
    NIET DENKEN AAN DE VISUALISATIE VAN HET SPEL, WEL AAN WAT NODIG IS OM GUI TE VORMEN -> light weight
 */
public class GameState implements Serializable {

    // counter die kijkt hoeveel tegels er huidig open liggen, gaat altijd van 0->1->2->0
    // tegelsFlipped is dus geen counter om te kijken hoeveel kaarten er gevonden zijn
    private int tegelsFlipped;

    // counter die gebruikt wordt om te bepalen wanneer het spel gedaan is
    private int aantalParenFound;

    // variabelen, om te controleren of de 2 kaartjes hetzelfde zijn
    private int vorigeTileUniqueId;
    private int huidigeTileUniqueId;

    private int gameId;
    private int aantalParen; // van het rooster
    private int aantalPerRij;// aantal tegels op 1 rij

    //usernames waarmee er ingelogd wordt
    private String naamSpelerA;
    private String naamSpelerB;

    //inboxes, die de spelers checken mbhv een thread.
    // een speler checkt enkel zijn eigen inbox
    private List<Commando> inboxSpelerA;
    private List<Commando> inboxSpelerB;

    //counters om de scores bij te houden
    private int aantalPuntenSpelerA;
    private int aantalPuntenSpelerB;

    private char aandeBeurt='A';

    //elke tegel moet bijgehouden worden, een tegel kan gewoon meerdere shit bevatten
    private ArrayList<Tile> tegelsList; // Tile = datastructuur voor in de AS, wordt dan geconvergeert naar VisualTile
                                        // in de client

    // default constructor
    public GameState() {
        System.out.println("gameState default constructor opgeroepen : niet de bedoeling");
        tegelsList = new ArrayList<Tile>();
        aantalPerRij = 0;
        aantalParen = 0;
    }

    /** normale constructor
     *
     * @param gameId
     * @param dimensions = 4 of 6, 4 staat voor 4x4, 6 voor 6X6
     * @param fotoSet = 'A' , 'B' of 'C'
     * @param hostName de naam van de speler die de game creert
     */
    public GameState(int gameId, int dimensions, char fotoSet, String hostName) {
        this.gameId=gameId;
        inboxSpelerA = new ArrayList<Commando>();
        inboxSpelerB = new ArrayList<Commando>();

        aantalParenFound =0;
        tegelsFlipped = 0;

        aantalPuntenSpelerA = 0;
        aantalPuntenSpelerB = 0;

        huidigeTileUniqueId = -1;
        vorigeTileUniqueId = -1;

        naamSpelerA = hostName;
        naamSpelerB="";

        aantalParen = dimensions * dimensions / 2;
        aantalPerRij = dimensions;

        //offset bepalen voor het inladen van de fotos
        int offset = 0;
        if (fotoSet == 'B') {
            offset = 100;
        } else if (fotoSet == 'C') {
            offset = 300;
        }

        tegelsList = new ArrayList<Tile>();

        int uniqueId = 1;

        /*  generatie van de Tiles en toevoeging dervan in de arraylist
            elke Tile heeft:
                - uniqueId, zodat we kunnen weten welke tile er geflipt moet worden
                - gewone id, deze wordt vergeleken om te kijken als we over dezelfde Tile spreken
                - een imageURL voor het inladen van de afbeelding uit de DB
                - een inmageURL '0' die de URL voor de achterkant van het kaartje voorstelt
        */
        for (int i = 0; i < aantalParen; i++) {

            tegelsList.add(new Tile(uniqueId, i, i + offset + 1 + "", "0"));
            uniqueId++;
            tegelsList.add(new Tile(uniqueId, i, i + offset + 1 + "", "0"));
            uniqueId++;

        }

        //de tegels shuffelen, zodat de positie random is
        Collections.shuffle(tegelsList);

        //de game is nu gecreeerd
    }

    public GameState(int gameId, int aantalParen, int aantalPerRij, String naamSpelerA, String naamSpelerB, int aantalPuntenSpelerA, int aantalPuntenSpelerB, char aandeBeurt, ArrayList<Tile> tegelsList, int aantalParenFound) {
        this.gameId=gameId;
        this.aantalParen = aantalParen;
        this.aantalPerRij = aantalPerRij;
        this.naamSpelerA = naamSpelerA;
        this.naamSpelerB = naamSpelerB;
        this.aantalPuntenSpelerA = aantalPuntenSpelerA;
        this.aantalPuntenSpelerB = aantalPuntenSpelerB;
        this.aandeBeurt = aandeBeurt;
        this.tegelsList = tegelsList;
        tegelsFlipped=0;
        this.aantalParenFound=aantalParenFound;
        this.inboxSpelerA= new ArrayList<>();
        this.inboxSpelerB=new ArrayList<>();
    }

    // aanpassing van de gameState als een 2e speler voor het eerst joint
    // todo: pas dit aan zodat mensen kunnen leaven en opnieuw joinen
    public void join(String secondUserName) {
        if(naamSpelerA!=null && naamSpelerB.equals("")){
            naamSpelerB = secondUserName;
            System.out.println("join in gameState succesvol");
        }

    }

    // nadat iets in de inbox van een speler is gekomen, wordt die speler getriggerd om zijn mailbox te bekijken en
    // dus dat commando op te halen

    /**
     * @param commando het commando dat de andere speler moet uitvoeren
     * @param activeUser de speler die het commando al reeds heeft uitgevoerd
     *
     *   als speler A deze methode triggert , dan:
     *                     wordt het commmando in speler B zijn mailbox gestopt
     *                     speler B haalt dit commando op en voert het lokaal uit
     *                     de GameState ( this ) wordt aangepast, daarin kunnen er extra messages
     *                     in de mailboxes gestopt worden
     */
    public synchronized void executeCommando(Commando commando, String activeUser) {

        //doorspelen van het commando naar de andere speler
        if (activeUser.equals(naamSpelerA)) {
            System.out.println("commando added in speler2 zijn inbox");
            inboxSpelerB.add(commando);
            notifyAll();
            System.out.println("executCommando notify in GameState");

        }
        else if (activeUser.equals(naamSpelerB)) {
            System.out.println("commando added in createSpeler zijn inbox");
            inboxSpelerA.add(commando);
            notifyAll();
            System.out.println("executCommando notify in GameState");

        }
        else {
            System.out.println("fout in executeCommando in GameState.java: geen commando added in mailbox andere pers");
        }

        //aanpassen van de gamestate:
        // doorgeven van de waarde zodat we de 2 laatste geklickte tegels kunnen bekijken
        vorigeTileUniqueId = huidigeTileUniqueId;
        huidigeTileUniqueId = commando.getUniqueTileId();
        tegelsFlipped++;

        //verwerking van wat er juist gebeurd en veranderd is, conclusies trekken ivm wat er moet gebeuren
        //indien nodig ook nog messages in de spelers hun mailboxen stoppen
        pasStateAan(commando, activeUser);

    }

    //verschil met vorige methode is dat dit commando door beide spelers moet uitgevoerd worden
    private synchronized void executeCommandoBoth(Commando commando){

        inboxSpelerA.add(commando);
        inboxSpelerB.add(commando);
        notifyAll();

    }


    /** deze methode wordt altijd opgeroepen nadat een speler op een kaartje gedrukt heeft
     * verwerking van de wat er juist gebeurd is
     *  bijvoorbeeld: als er 2 kaartjes open liggen:
     *      kijken als het dezelfde zijn
     *              indien ja:
     *                      speler een punt geven + message naar beide spelers hiervoor
     *                      kaartjes locken, (unclickable maken)
     *              indien nee:
     *                      kaartjes terug omdraaien + message naar beide spelers
     *                      wisselen van speler
     *
     * @param commando het commando die juist is uitgevoerd op de client
     * @param activeUser de client die dit commando uitvoert
     */
    private void pasStateAan(Commando commando, String activeUser) {

        //opvragen van de overeenkomstige Tile
        Tile huidigeTile = getTileMetUniqueId(commando.getUniqueTileId());

        //state aanpassen, dit kaartje is nu omgedraaid (in de arrayList)
        huidigeTile.setFlippedOver(true);

        // lock zetten op de tile, zodat je na hem open te klikken er niet nog eens kan op duwen
        // dus de tile wordt dan unclickable
        executeCommandoBoth(new Commando("LOCK", huidigeTile.getUniqueIdentifier()));


        // als je beurt over is (en dus 2 tegels hebt opgengeklikt
        if(tegelsFlipped == 2){

            System.out.println("verwerkig in appserver started");

            //vorigeTile en huidigeTile zijn de 2 Tiles die aangeklikt zijn in de speler zijn beurt
            Tile vorigeTile = getTileMetUniqueId(vorigeTileUniqueId);


            // als hun id (dus niet hun uniqueId) hetzelfde is -> zelfde kaartjes gevonden
            if(huidigeTile.getId() == vorigeTile.getId()){


                // punt toekennen aan huidige speler
                System.out.println("award 1 punt aan een speler");
                awardPuntTo(activeUser);

                //pas state aan van de tegels
                huidigeTile.setFound(true);
                vorigeTile.setFound(true);

                //lock zetten op de net gevonden tegels in beide spelers, zodat ze er niet meer kunnen op klikken
                executeCommandoBoth(new Commando("LOCK", huidigeTile.getUniqueIdentifier()));
                executeCommandoBoth(new Commando("LOCK", vorigeTile.getUniqueIdentifier()));

                //counter verhogen
                aantalParenFound++;


                //als alle tegels gevonden zijn
                if(aantalParenFound == aantalParen){


                    //dan is het spel gedaan
                    //wie is de winnaar?

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
                //als de 2 tegels die de speler omgedraaid heeft dus niet dezelfde zijn

                //pas state terug aan
                vorigeTile.setFlippedOver(false);
                huidigeTile.setFlippedOver(false);

                //draai beide tegels terug om (in beide spelers)
                executeCommandoBoth(new Commando("UNFLIP", vorigeTile.getUniqueIdentifier()));
                executeCommandoBoth(new Commando("UNFLIP", huidigeTile.getUniqueIdentifier()));

                //zorgen dat deze tegels terug clickable zijn
                executeCommandoBoth(new Commando("UNLOCK", vorigeTile.getUniqueIdentifier()));
                executeCommandoBoth(new Commando("UNLOCK", huidigeTile.getUniqueIdentifier()));

                //de beurt is nu aan de volgende speler
                executeCommandoBoth(new Commando("SWITCH", 1));// 1 is testwaarde, zodat we hem toch gaan vinden

                if(aandeBeurt=='A'){
                    aandeBeurt='B';
                }
                else{
                    aandeBeurt='A';
                }

            }


            //reset de counter die telt hoeveel tegels er openliggen
            tegelsFlipped = 0;
        }
        notifyAll();

    }


    /** methode die enkel in pasGameStateAan wordt opgeroepen
     *  kent de speler een punt toe, en laat dit aanpassen in beide spelers
     *
     * @param activeUser de actieve speler (die het punt dus verdient)
     */
    private void awardPuntTo(String activeUser) {


        if(activeUser.equals(naamSpelerA)){
            aantalPuntenSpelerA ++;        //deze 1 is een dummywaarde
            inboxSpelerA.add(new Commando("AWARDTOME",  1));
            inboxSpelerB.add(new Commando("AWARDTOYOU", 1));
        }

        else if(activeUser.equals(naamSpelerB)){
            aantalPuntenSpelerB ++;
            inboxSpelerB.add(new Commando("AWARDTOME",  1));
            inboxSpelerA.add(new Commando("AWARDTOYOU", 1));
        }

        else{
            System.out.println("fout in awardPuntTo : GameState");
        }

    }


    /**
     * gaat in de arraylist met tiles van de gamestate de tile opvragen met de juiste id
     * @param uniqueTileId
     * @return
     */
    private Tile getTileMetUniqueId( int uniqueTileId){

        for (Tile tile : tegelsList) {
            if(tile.getUniqueIdentifier() == uniqueTileId){ return tile;}
        }

        System.out.println("fout in GameState.java : getTileMetUniqeId");
        return null;
    }


    /** dient om de specifieke client ZIJN inbox te geven
     *
     * @param userName
     * @return
     */
    public synchronized List<Commando> getInbox(String userName) {

       // System.out.println("GameState: getInbox door user"+userName);
        if (userName.equals(naamSpelerA)) {

            while (inboxSpelerA.isEmpty()) {
                try {
                    //System.out.println("inbox is leeg, wait started");
                    wait();
                    //System.out.println("iets nieuws in de inbox van user " + naamSpelerA + ": wait stopped");
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            List<Commando> inbox = new ArrayList<Commando>(inboxSpelerA);
            inboxSpelerA.clear();
            //System.out.println("inbox met grootte :"+inbox.size()+" gereturned");
            //miss moet de inbox nu leeggemaakt worden
            return inbox;

        } else if (userName.equals(naamSpelerB)) {

            while (inboxSpelerB.isEmpty()) {
                try {
                    //System.out.println("inbox is leeg, wait started");
                    wait();
                    //System.out.println("iets nieuws in de inbox van usser"+naamSpelerB+" : wait stopped");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            List<Commando> inbox = new ArrayList<Commando>(inboxSpelerB);
            inboxSpelerB.clear();
          //  System.out.println("inbox met grootte :"+inbox.size()+" gereturned");
            //miss moet de inbox nu leeggemaakt worden
            return inbox;

        }
        System.out.println("fout in getInbox in GameState.java");
        return null;
    }

    //getters
    public int getAantalParen() {
        return aantalParen;
    }

    public int getAantalPerRij() {
        return aantalPerRij;
    }

    public ArrayList<Tile> getTegelsList() {
        return tegelsList;
    }

    public int getAantalPuntenSpelerA() {
        return aantalPuntenSpelerA;
    }

    public void setAantalPuntenSpelerA(int aantalPuntenSpelerA) {
        this.aantalPuntenSpelerA = aantalPuntenSpelerA;
    }

    public int getAantalPuntenSpelerB() {
        return aantalPuntenSpelerB;
    }

    public void setAantalPuntenSpelerB(int aantalPuntenSpelerB) {
        this.aantalPuntenSpelerB = aantalPuntenSpelerB;
    }

    public void setTegelsList(ArrayList<Tile> tegelsList) {
        this.tegelsList = tegelsList;
    }

    public char getAandeBeurt() {
        return aandeBeurt;
    }

    public void setAandeBeurt(char aandeBeurt) {
        this.aandeBeurt = aandeBeurt;
    }

    public synchronized boolean changeInTurn(char userTurn) {
        while(userTurn==this.aandeBeurt){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("change turn");
        return true;
    }

    public int getGameId() {
        return gameId;
    }

    public String getNaamSpelerA() {
        return naamSpelerA;
    }

    public void setNaamSpelerA(String naamSpelerA) {
        this.naamSpelerA = naamSpelerA;
    }

    public String getNaamSpelerB() {
        return naamSpelerB;
    }

    public void setNaamSpelerB(String naamSpelerB) {
        this.naamSpelerB = naamSpelerB;
    }

    public int getAantalParenFound() {
        return aantalParenFound;
    }
}