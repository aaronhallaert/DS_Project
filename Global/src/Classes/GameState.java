package Classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    //usernames waarmee er ingelogd wordt -> in een list
    private ArrayList<String> spelers;

    //inboxes, die de spelers checken mbhv een thread.
    // een speler checkt enkel zijn eigen inbox -> in een list
    private HashMap<String, List<Commando>> inbox;

    private int aantalSpelers;
    private ArrayList<String> spectators;
    private HashMap<String, List<Commando>> inboxSpectators;

    //counters om de scores bij te houden -> in een list
    private HashMap<String, Integer> punten;

    private String aandeBeurt;

    //elke tegel moet bijgehouden worden, een tegel kan gewoon meerdere shit bevatten
    private ArrayList<Tile> tegelsList; // Tile = datastructuur voor in de AS, wordt dan geconvergeert naar VisualTile
    // in de client

    private boolean finished;

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
    public GameState(int gameId, int dimensions, char fotoSet, String hostName, int aantalSpelers) {
        this.gameId=gameId;


        this.aantalSpelers=aantalSpelers;

        /* initialize gamestate ---------*/
        inbox= new HashMap<>();
        spelers= new ArrayList<>();
        punten= new HashMap<>();
        inboxSpectators = new HashMap<>();
        tegelsList = new ArrayList<Tile>();
        spectators= new ArrayList<>();

        aantalParenFound =0;
        tegelsFlipped = 0;

        huidigeTileUniqueId = -1;
        vorigeTileUniqueId = -1;

        finished = false;

        /* toevoegen van user die game maakt ---------*/
        spelers.add(hostName);
        punten.put(hostName, 0);
        inbox.put(hostName, new ArrayList<>());
        aandeBeurt=hostName;

        /* kenmerken game --------------------*/
        aantalParen = dimensions * dimensions / 2;
        aantalPerRij = dimensions;


        // offset bepalen voor het inladen van de fotos
        int offset = 0;
        if (fotoSet == 'B') {
            offset = 100;
        } else if (fotoSet == 'C') {
            offset = 300;
        }

        /* init tegels --------------------*/

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

        // de tegels shuffelen, zodat de positie random is
        Collections.shuffle(tegelsList);

        // GAME CREATED
    }

    public GameState(int gameId, int aantalParen, int aantalPerRij, ArrayList<String> spelers, HashMap<String, Integer> punten, String aandeBeurt, ArrayList<Tile> tegelsList, int aantalParenFound) {
        this.gameId=gameId;
        this.aantalParen = aantalParen;
        this.aantalPerRij = aantalPerRij;
        this.spelers=spelers;
        this.punten=punten;
        this.aandeBeurt = aandeBeurt;
        this.tegelsList = tegelsList;
        tegelsFlipped=0;
        this.aantalParenFound=aantalParenFound;
        this.aantalSpelers= 2;
        this.inbox= new HashMap<>();
        this.inboxSpectators=new HashMap<>();
    }

    // aanpassing van de gameState als een 2e speler voor het eerst joint
    public void join(String secondUserName) {
        if(spelers.size()<=aantalSpelers && spelers.size()>0 && !spelers.contains(secondUserName)){
            spelers.add(secondUserName);
            punten.put(secondUserName, 0);
            inbox.put(secondUserName, new ArrayList<>());
            System.out.println("join in gamestate succesvol");
        }

    }

    // nadat iets in de inbox van een speler is gekomen, wordt die speler getriggerd om zijn mailbox te bekijken en
    // dus dat commando op te halen

    /**
     * @param commando het commando dat de andere speler moet uitvoeren
     * @param activeUser de speler die het commando al reeds heeft uitgevoerd
     * @return returnt true indien na dit commando spelers van beurt wisselen
     *   als speler A deze methode triggert , dan:
     *                     wordt het commmando in speler B zijn mailbox gestopt
     *                     speler B haalt dit commando op en voert het lokaal uit
     *                     de GameState ( this ) wordt aangepast, daarin kunnen er extra messages
     *                     in de mailboxes gestopt worden
     */
    public synchronized boolean executeCommando(Commando commando, String activeUser) {

        for (String speler : spelers) {
            if (!speler.equals(activeUser)){
               // System.out.println("commando added in "+ speler +" zijn inbox");
                if(inbox.get(speler) != null) {
                    inbox.get(speler).add(commando);
                }
                else{
                    inbox.put(speler, new ArrayList<>());
                    inbox.get(speler).add(commando);
                }
            }
        }
        for (String spectator : spectators) {
            if(inboxSpectators.get(spectator) != null) {
                inboxSpectators.get(spectator).add(commando);
            }
            else{
                inboxSpectators.put(spectator, new ArrayList<>());
                inboxSpectators.get(spectator).add(commando);
            }
        }
        notifyAll();


        //aanpassen van de gamestate:
        // doorgeven van de waarde zodat we de 2 laatste geklickte tegels kunnen bekijken
        vorigeTileUniqueId = huidigeTileUniqueId;
        huidigeTileUniqueId = commando.getUniqueTileId();
        tegelsFlipped++;

        //verwerking van wat er juist gebeurd en veranderd is, conclusies trekken ivm wat er moet gebeuren
        //indien nodig ook nog messages in de spelers hun mailboxen stoppen
        return pasStateAan(commando, activeUser);

    }

    //verschil met vorige methode is dat dit commando door alle spelers + spectators moet uitgevoerd worden
    private synchronized void executeCommandoToAll(Commando commando){

        for (List<Commando> commandoList : inbox.values()) {
            commandoList.add(commando);
        }

        for (List<Commando> commandoList : inboxSpectators.values()) {
            commandoList.add(commando);
        }


        notifyAll();

    }


    @Override
    public String toString() {
        return "GameState{" +
                "tegelsFlipped=" + tegelsFlipped +
                ", aantalParenFound=" + aantalParenFound +
                ", spelers=" + spelers +
                ", aandeBeurt='" + aandeBeurt + '\'' +
                '}';
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
     * @return boolean die true teruggeeft wanneer backup upgedate moet worden
     */
    private boolean pasStateAan(Commando commando, String activeUser) {

        //opvragen van de overeenkomstige Tile
        Tile huidigeTile = getTileMetUniqueId(commando.getUniqueTileId());

        //state aanpassen, dit kaartje is nu omgedraaid (in de arrayList)
        huidigeTile.setFlippedOver(true);

        // lock zetten op de tile, zodat je na hem open te klikken er niet nog eens kan op duwen
        // dus de tile wordt dan unclickable
        executeCommandoToAll(new Commando("LOCK", huidigeTile.getUniqueIdentifier()));


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
                executeCommandoToAll(new Commando("LOCK", huidigeTile.getUniqueIdentifier()));
                executeCommandoToAll(new Commando("LOCK", vorigeTile.getUniqueIdentifier()));

                //counter verhogen
                aantalParenFound++;


                //als alle tegels gevonden zijn
                if(aantalParenFound == aantalParen){

                    finished = true;

                    //dan is het spel gedaan
                    //wie is de winnaar?

                    ArrayList<String> winners= new ArrayList<>();

                    int max= Collections.max(punten.values());

                    for (String speler : punten.keySet()) {
                        int punt= punten.get(speler);
                        if(punt == max){
                            winners.add(speler);
                        }
                    }

                    if(winners.size()==1){
                        String winner= winners.get(0);

                        for (String speler : spelers) {
                            if(!speler.equals(winner)){
                                inbox.get(speler).add(new Commando("LOSS",1));
                            }
                            else{
                                inbox.get(speler).add(new Commando("WIN", 1));
                            }
                        }

                    }
                    else{
                        for (String speler : spelers) {
                            if (!winners.contains(speler)) {
                                inbox.get(speler).add(new Commando("LOSS", 1));
                            }
                        }
                        for (String winner : winners) {
                            inbox.get(winner).add(new Commando("DRAW", 1));
                        }
                    }

                    notifyAll();


                }



            }else{
                //als de 2 tegels die de speler omgedraaid heeft dus niet dezelfde zijn

                //pas state terug aan
                vorigeTile.setFlippedOver(false);
                huidigeTile.setFlippedOver(false);

                //draai beide tegels terug om (in beide spelers)
                executeCommandoToAll(new Commando("UNFLIP", vorigeTile.getUniqueIdentifier()));
                executeCommandoToAll(new Commando("UNFLIP", huidigeTile.getUniqueIdentifier()));

                //zorgen dat deze tegels terug clickable zijn
                executeCommandoToAll(new Commando("UNLOCK", vorigeTile.getUniqueIdentifier()));
                executeCommandoToAll(new Commando("UNLOCK", huidigeTile.getUniqueIdentifier()));

                //de beurt is nu aan de volgende speler
                executeCommandoToAll(new Commando("SWITCH", 1));// 1 is testwaarde, zodat we hem toch gaan vinden


                for (int i = 0; i < spelers.size(); i++) {
                    String speler= spelers.get(i);
                    if(aandeBeurt.equals(speler)){
                        if(i!=spelers.size()-1) {
                            aandeBeurt = spelers.get(i + 1);
                        }
                        else{
                            aandeBeurt= spelers.get(0);
                        }
                        break;
                    }
                }

                //reset de counter die telt hoeveel tegels er openliggen
                tegelsFlipped = 0;
                notifyAll();
                return true;
            }


            //reset de counter die telt hoeveel tegels er openliggen
            tegelsFlipped = 0;
        }
        notifyAll();
        return false;

    }


    /** methode die enkel in pasGameStateAan wordt opgeroepen
     *  kent de speler een punt toe, en laat dit aanpassen in beide spelers
     *
     * @param activeUser de actieve speler (die het punt dus verdient)
     */
    private void awardPuntTo(String activeUser) {

        int oldPunten= punten.get(activeUser);
        punten.put(activeUser, oldPunten+1);

        executeCommandoToAll(new Commando("AWARD", activeUser, 1));




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

        if(spelers.contains(userName)) {
            while (inbox.get(userName).isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            List<Commando> inboxSpeler = new ArrayList<>(inbox.get(userName));
            inbox.get(userName).clear();

            return inboxSpeler;
        }
        else{

            inboxSpectators.putIfAbsent(userName, new ArrayList<>());

            while (inboxSpectators.get(userName).isEmpty()) {
                try {
                    //System.out.println("inbox is leeg, wait started");
                    wait();
                    //System.out.println("iets nieuws in de inbox van usser"+naamSpelerB+" : wait stopped");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            List<Commando> inboxSpectator = new ArrayList<Commando>(inboxSpectators.get(userName));
            inboxSpectators.get(userName).clear();
            return inboxSpectator;
        }

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



    public void setTegelsList(ArrayList<Tile> tegelsList) {
        this.tegelsList = tegelsList;
    }

    public String getAandeBeurt() {
        return aandeBeurt;
    }

    public void setAandeBeurt(String aandeBeurt) {
        this.aandeBeurt = aandeBeurt;
    }

    public synchronized boolean changeInTurn(String userTurn) {
        while(userTurn.equals(this.aandeBeurt)){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public int getGameId() {
        return gameId;
    }

    public int getAantalParenFound() {
        return aantalParenFound;
    }

    public boolean getfinished() { return finished;
    }

    public int getTegelsFlipped() {
        return tegelsFlipped;
    }

    public void setTegelsFlipped(int tegelsFlipped) {
        this.tegelsFlipped = tegelsFlipped;
    }

    public void setAantalParenFound(int aantalParenFound) {
        this.aantalParenFound = aantalParenFound;
    }

    public int getVorigeTileUniqueId() {
        return vorigeTileUniqueId;
    }

    public void setVorigeTileUniqueId(int vorigeTileUniqueId) {
        this.vorigeTileUniqueId = vorigeTileUniqueId;
    }

    public int getHuidigeTileUniqueId() {
        return huidigeTileUniqueId;
    }

    public void setHuidigeTileUniqueId(int huidigeTileUniqueId) {
        this.huidigeTileUniqueId = huidigeTileUniqueId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public void setAantalParen(int aantalParen) {
        this.aantalParen = aantalParen;
    }

    public void setAantalPerRij(int aantalPerRij) {
        this.aantalPerRij = aantalPerRij;
    }

    public ArrayList<String> getSpelers() {
        return spelers;
    }

    public void setSpelers(ArrayList<String> spelers) {
        this.spelers = spelers;
    }

    public HashMap<String, List<Commando>> getInbox() {
        return inbox;
    }

    public void setInbox(HashMap<String, List<Commando>> inbox) {
        this.inbox = inbox;
    }

    public int getAantalSpelers() {
        return aantalSpelers;
    }

    public void setAantalSpelers(int aantalSpelers) {
        this.aantalSpelers = aantalSpelers;
    }

    public ArrayList<String> getSpectators() {
        return spectators;
    }

    public void setSpectators(ArrayList<String> spectators) {
        this.spectators = spectators;
    }

    public HashMap<String, List<Commando>> getInboxSpectators() {
        return inboxSpectators;
    }

    public void setInboxSpectators(HashMap<String, List<Commando>> inboxSpectators) {
        this.inboxSpectators = inboxSpectators;
    }

    public HashMap<String, Integer> getPunten() {
        return punten;
    }

    public void setPunten(HashMap<String, Integer> punten) {
        this.punten = punten;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}