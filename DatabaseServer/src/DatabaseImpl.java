import Classes.Game;
import Classes.Score;
import Classes.GameInfo;
import interfaces.DatabaseInterface;
import com.google.common.hash.Hashing;


import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseImpl extends UnicastRemoteObject implements DatabaseInterface {

    /*---------ATTRIBUTES --------------------*/
    private String databaseNaam;
    private static Connection conn = null;
    private ArrayList<DatabaseInterface> otherDbs=new ArrayList<>();
    private List<GameInfo> gameInfoList=new ArrayList<>();

    /*--------- CONSTRUCTOR ----------------*/
    public DatabaseImpl(String databaseNaam) throws RemoteException{
        this.databaseNaam = databaseNaam;
        // maakt connectie met sql database
        connect();
    }


    /*--------- OWN METHODS ----------------*/
    /**
     * connectie met databank maken
     */
    public void connect() {

        // SQLite connection string
        String workingDir = System.getProperty("user.dir");
        String url = "jdbc:sqlite:"+workingDir+"\\DatabaseServer\\data\\"+databaseNaam;

        try {
            if(conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(url);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * close the connection to the database
     */
    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static String hash(String password, String salt){
        return Hashing.sha256().hashString((password + salt),StandardCharsets.UTF_8).toString();
    }
    private static String hash(String password){
        return Hashing.sha256().hashString((password),StandardCharsets.UTF_8).toString();
    }

    private boolean vergelijkGameInfoList(List<GameInfo> oudeList, List<GameInfo> gameInfoList){
        if(oudeList.size()!=gameInfoList.size()){
            return false;
        }
        else{
            for (GameInfo gameInfo : gameInfoList) {
                GameInfo foundGameInfo = null;

                for (GameInfo info : oudeList) {
                    if(info.getGameId()==gameInfo.getGameId()){
                        foundGameInfo =info;

                        if(info.getSpelers().size()!=gameInfo.getSpelers().size()){
                            return false;
                        }

                        break;
                    }
                }
                if(foundGameInfo == null){
                    return false;
                }
            }
        }

        return true;
    }




    /*--------- SERVICES --------------------*/

    // USER //
    /**
     * check of username al bestaat
     * @param name
     * @return
     */
    @Override
    public boolean userNameExists(String name){
        String sql = "SELECT Username FROM Persons WHERE Username=?";

        connect();
        try (
                PreparedStatement pstmt  = conn.prepareStatement(sql)){
            pstmt.setString(1, name);

            ResultSet rs  = pstmt.executeQuery();
            List<String> users= new ArrayList<>();

            while(rs.next()){
                users.add(rs.getString("Username"));
            }

            if (users.size()>0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in usernameExists");
        }

        closeConnection();
        return false;
    }
    /**
     * Insert a new row into the users table
     *
     * @param name
     * @param password
     */
    @Override
    public void insertUser(String name, String password, boolean onMaster) {
        String sql = "INSERT INTO Persons(Username,Password, Salt) VALUES(?,?,?)";
        String salt=hash((System.currentTimeMillis()+"RandomString"));
        String hashedPaswoord= hash(password, salt);
        connect();

        try (

                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, hashedPaswoord);
            pstmt.setString(3, salt);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        closeConnection();

        if(onMaster){
            for (DatabaseInterface dbRef : DataServerMain.pollToOtherDBs.getDBRefs()) {

                new Thread(()-> {
                    try {
                        dbRef.insertUser(name, password, false);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }).start();

            }
        }
    }

    // CREDENTIALS //
    /**
     * checkt user credentials
     * @param naam
     * @param paswoord
     * @return
     * @throws RemoteException
     */
    @Override
    public boolean checkUserCred(String naam, String paswoord) throws RemoteException {

        String sql = "SELECT Password, Salt FROM Persons WHERE Username=?";

        //fire up the connection
        connect();

        boolean result = false;

        try (
            // opvragen van password en salt in db
            PreparedStatement pstmt  = conn.prepareStatement(sql)){
            pstmt.setString(1, naam);
            ResultSet rs  = pstmt.executeQuery();
            String retrievePassword="";
            String retrieveSalt="";
            // loop through the result set
            while (rs.next()) {
                retrievePassword = rs.getString("Password");
                retrieveSalt= rs.getString("Salt");
            }


            // effectieve controle credentials
            if (retrievePassword.equals(hash(paswoord, retrieveSalt))) {
                result = true;
            } else {
                result = false;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        // connectie met databank weer sluiten
        closeConnection();

        return result;
    }
    /**
     * aanmaken van token (hash van paswoord en de huidige tijd in ms)
     * @param username
     * @param password
     * @throws RemoteException
     */
    @Override
    public void createToken(String username, String password, boolean onMaster)throws RemoteException {
        connect();
        // enkel als credentials juist zijn
        if(checkUserCred(username, password)){

            // update query
            String sql="UPDATE Persons SET token=?, token_timestamp=? WHERE Username= ?;";
            String token= hash(password+System.currentTimeMillis());
            connect();
            try{

                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, token);
                pstmt.setLong(2, System.currentTimeMillis());
                pstmt.setString(3, username);
                pstmt.executeUpdate();
            }
            catch (SQLException se){
                se.printStackTrace();
            }

        }

        closeConnection();
        if(onMaster){
            for (DatabaseInterface dbRef : DataServerMain.pollToOtherDBs.getDBRefs()) {
                new Thread(() -> {
                    try {
                        dbRef.createToken(username, password, false);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }).start();

            }
        }
    }
    /**
     * checkt of token niet vervallen is (minder dan 24u)
     * @param username
     * @param token
     * @return
     * @throws RemoteException
     */
    @Override
    public boolean isTokenValid(String username, String token) throws RemoteException{
        connect();
        String sql = "SELECT token_timestamp FROM Persons WHERE username = ? AND token = ?";
        try{

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,username);
            pstmt.setString(2, token);
            ResultSet rs = pstmt.executeQuery();
            long currentTime = System.currentTimeMillis();
            while(rs.next()){
                // 24 uur controle
                if(currentTime - rs.getLong("token_timestamp") < 86400000){
                    return true;
                }
            }

            closeConnection();
            return false;
        }
        catch(SQLException se){
            closeConnection();
            System.out.println(se.getMessage());
            se.printStackTrace();

            return false;
        }

    }
    /**
     * token timestamp op 0 zetten in db
     * @param username naam van de actieve gebruiker
     * @throws RemoteException
     */
    @Override
    public void cancelToken(String username, boolean onMaster) throws RemoteException{
        String sql = "UPDATE Persons SET token_timestamp=? WHERE Username= ?;";
        connect();
        try{

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,"0");
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
        catch (SQLException se){
            se.printStackTrace();
        }
        closeConnection();

        if(onMaster) {
            for (DatabaseInterface dbRef : DataServerMain.pollToOtherDBs.getDBRefs()) {

                new Thread(()-> {
                    try {
                        dbRef.cancelToken(username, false);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }
    /**
     * opvragen van token
     * @param username
     * @return
     * @throws RemoteException
     */
    @Override
    public String getToken(String username) throws RemoteException{
        String sql = "SELECT token FROM Persons WHERE Username = ? ";

        connect();
        try{

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,username);
            ResultSet rs = pstmt.executeQuery();
            String s= rs.getString("token");
            closeConnection();
            return s;

        }
        catch(SQLException se){
            System.out.println(se.getMessage());
            se.printStackTrace();
            closeConnection();
            return null;
        }

    }


    // IMAGES //
    /**
     * effectief de array van bytes in de db opvragen
     * @param afbeeldingId de naam van de afbeelding
     * @return de array van bytes
     * @throws RemoteException
     */
    @Override
    public byte[] getImage(String afbeeldingId) throws RemoteException { //in 0 zit de achterkant van de foto

        String sql = "SELECT image FROM pictures WHERE naam=?";

        byte[] array = null;

        connect();

        try {

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, afbeeldingId);
            ResultSet rs = pstmt.executeQuery();
            array = rs.getBytes("image");
            rs.close();
            pstmt.close();

        }

        catch (SQLException e) {
            e.printStackTrace();
        }

        closeConnection();
        return array;
    }
    /**
     * effectief de array van bytes in de db opslaan
     * @param afbeeldingId
     * @param afbeelding
     * @throws RemoteException
     */
    @Override
    public void storeImage(String afbeeldingId, byte[] afbeelding, boolean onMaster) throws RemoteException{

        System.out.println("database storen van een image started");

        String sql = "INSERT INTO pictures(naam,image) VALUES(?,?)";

        connect();

        try {

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, afbeeldingId);
            pstmt.setBytes(2, afbeelding);
            pstmt.executeUpdate();
            pstmt.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        closeConnection();

        if(onMaster){
            for (DatabaseInterface dbRef : DataServerMain.pollToOtherDBs.getDBRefs()) {

                new Thread(() -> {
                    try {
                        dbRef.storeImage(afbeeldingId, afbeelding, false);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }).start();

            }
        }

    }

    // CONNECTIONS //
    @Override
    public void connectTo(DatabaseInterface toImpl) throws RemoteException {
        otherDbs.add(toImpl);
    }


    // GAMES //
    @Override
    public synchronized void updateGameInfo(GameInfo gameInfo, boolean onMaster) {
        connect();
        ArrayList<String> spelers= gameInfo.getSpelers();
        StringBuilder sb= new StringBuilder();
        for (int i = 0; i < spelers.size(); i++) {
            sb.append(spelers.get(i));
            if(i!= spelers.size()-1){
                sb.append(", ");
            }
        }
        int aantalSpelersConnected=gameInfo.getAantalSpelersConnected();
        int gameId=gameInfo.getGameId();
        int appserverpoort=gameInfo.getAppServerPoort();

        try {
            // update gameInfo
            String updateGame = "UPDATE GameInfo SET aantalSpelersConnected=?, spelers=?, appserverpoort=? WHERE gameId= ?;";
            PreparedStatement pstmtUpdate = conn.prepareStatement(updateGame);

            pstmtUpdate.setInt(1, aantalSpelersConnected);
            pstmtUpdate.setString(2, sb.toString());
            pstmtUpdate.setInt(3, appserverpoort);

            pstmtUpdate.setInt(4, gameId);
            pstmtUpdate.executeUpdate();
        }
        catch (SQLException se){
            se.printStackTrace();
        }
        closeConnection();
        for (GameInfo info : gameInfoList) {
            if(info.getGameId()==gameInfo.getGameId()){
                gameInfoList.remove(info);
                gameInfoList.add(gameInfo);
                break;
            }
        }
        notifyAll();
        if(onMaster) {
            for (DatabaseInterface dbRef : DataServerMain.pollToOtherDBs.getDBRefs()) {
            new Thread(() -> {
                try {
                    dbRef.updateGameInfo(gameInfo, false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }).start();
            }
        }
    }

    @Override
    public List<GameInfo> getGameInfoList() throws RemoteException {
        connect();
        String getGameInfoList= "SELECT * FROM GameInfo";
        gameInfoList=new ArrayList<>();
        try {
            PreparedStatement pstmt= conn.prepareStatement(getGameInfoList);
            ResultSet rs= pstmt.executeQuery();
            while(rs.next()){
                gameInfoList.add(
                        new GameInfo(rs.getInt("gameId"),
                                rs.getString("spelers"),
                                rs.getInt("aantalSpelers"),
                                rs.getInt("aantalSpelersConnected"),
                                rs.getString("fotoSet"),
                                rs.getInt("roosterSize"),
                                rs.getInt("appServerPoort")
                        )
                );
            }
            closeConnection();
            return gameInfoList;

        } catch (SQLException e) {
            e.printStackTrace();

            return null;
        }
    }

    @Override
    public synchronized List<GameInfo> getGameInfoList(boolean dummy) throws RemoteException {
        ArrayList<GameInfo> gameInfoOld= new ArrayList<>(gameInfoList);
        while(vergelijkGameInfoList(gameInfoOld, gameInfoList) ) {
            try {
                wait();
                System.out.println("wait wordt verbroken");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return gameInfoList;

    }

    @Override
    public synchronized void addGameInfo(GameInfo gameInfo, boolean onMaster) throws RemoteException {


        System.out.println("game info toevoegen");
        ArrayList<String> spelers= gameInfo.getSpelers();
        StringBuilder sb= new StringBuilder();
        for (int i = 0; i < spelers.size(); i++) {
            sb.append(spelers.get(i));
            if(i!= spelers.size()-1){
                sb.append(", ");
            }
        }


        int aantalSpelersConnected=gameInfo.getAantalSpelersConnected();
        int aantalSpelers =gameInfo.getAantalSpelers();
        int roosterSize= gameInfo.getRoosterSize();
        String fotoset= gameInfo.getFotoSet();
        int gameId=gameInfo.getGameId();
        int appserverpoort=gameInfo.getAppServerPoort();

        String sql="SELECT * from GameInfo where gameId=?;";

        try {
            connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1,gameId);
            ResultSet rs = pstmt.executeQuery();
            if(!rs.next()){
                // voeg toe
                String addGame="INSERT INTO GameInfo(gameId, aantalSpelersConnected, fotoSet, roosterSize, spelers, aantalSpelers, appserverpoort) VALUES(?,?,?,?,?,?,?)";
                PreparedStatement pstmtAdd=conn.prepareStatement(addGame);
                pstmtAdd.setInt(1, gameId);
                pstmtAdd.setInt(2, aantalSpelersConnected);
                pstmtAdd.setString(3, fotoset);
                pstmtAdd.setInt(4, roosterSize);
                pstmtAdd.setString(5, sb.toString());
                pstmtAdd.setInt(6, aantalSpelers);
                pstmtAdd.setInt(7, appserverpoort);

                pstmtAdd.executeUpdate();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        closeConnection();

        if(onMaster){
            for (DatabaseInterface dbRef : DataServerMain.pollToOtherDBs.getDBRefs()) {
                new Thread(() -> {
                    //Do whatever
                    try {
                        dbRef.addGameInfo(gameInfo, false);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }

        System.out.println("toevoegen van gameinfo in list");
        gameInfoList.add(gameInfo);
        notifyAll();

    }

    @Override
    public synchronized void deleteGameInfo(int gameId, boolean onMaster) throws RemoteException {

        String sql = "DELETE FROM GameInfo WHERE gameId=?;";

        try {
            connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, gameId);
            pstmt.executeUpdate();

        }

        catch (SQLException e) {
            e.printStackTrace();
        }

        closeConnection();

        //broadcast naar de andere databanken
        if(onMaster){
            for (DatabaseInterface dbRef : DataServerMain.pollToOtherDBs.getDBRefs()) {
                new Thread(() -> {
                    try {
                        dbRef.deleteGameInfo(gameId, false);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }

        for (GameInfo gameInfo : gameInfoList) {
            if(gameInfo.getGameId()==gameId){
                gameInfoList.remove(gameInfo);


                break;
            }
        }
        notifyAll();
        System.out.println("game met gameId: "+gameId+"succesvol verwijderd uit DB");

    }

    // SCORETABEL //

    @Override
    public boolean hasScoreRij(String username) throws RemoteException{

        String sql = "SELECT Username FROM Scores WHERE Username = ? ";

        connect();

        boolean returner = false;

        try{
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,username);
            ResultSet rs = pstmt.executeQuery();

            if(rs.getString("Username").equals(username)){
                returner = true;
            }
            else {
                returner = false;
            }




        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        closeConnection();
        return returner;
    }

    @Override
    public ArrayList<Score> getScores() throws RemoteException {

        String sql = "SELECT * FROM Scores";

        connect();

        ArrayList<Score> scoreList = new ArrayList<>();

        try {

            PreparedStatement pstmt = conn.prepareStatement(sql);

            // de ganse tabel zit in rs nu
            ResultSet rs = pstmt.executeQuery();

            //zolang dat er rows gevonden worden
            while(rs.next()){
                String naam = rs.getString("Username");
                int wins = rs.getInt("wins");
                int draws = rs.getInt("draws");
                int losses = rs.getInt("losses");
                int max4x4 = rs.getInt("max4x4");
                int max6x6 = rs.getInt("max6x6");
                int aantalGames = rs.getInt("aantalGames");

                scoreList.add(new Score(naam, wins, draws, losses, max4x4, max6x6, aantalGames));
            }




        closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return scoreList;

    }



    @Override
    public void insertScoreRow(String username, boolean onMaster) throws RemoteException {

        System.out.println("rij toegevoegd voor user" + username +" in scorelijst");


        String sql = "INSERT INTO Scores(Username,wins,draws,losses,max4x4,max6x6,aantalGames) VALUES(?,?,?,?,?,?,?)";

        connect();

        try {

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setInt(2,0);
            pstmt.setInt(3,0);
            pstmt.setInt(4,0);
            pstmt.setInt(5,0);
            pstmt.setInt(6,0);
            pstmt.setInt(7,0);

            pstmt.executeUpdate();
            pstmt.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        closeConnection();

        if(onMaster){
            for (DatabaseInterface dbRef : DataServerMain.pollToOtherDBs.getDBRefs()) {
                new Thread(() -> {
                    try {
                        dbRef.insertScoreRow(username, false);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }).start();

            }
        }


    }

    @Override
    public synchronized void updateScores(String username, int roosterSize, int eindScore, String command, boolean onMaster) throws RemoteException {

        String sql = "SELECT * FROM Scores WHERE Username = ? ";

        ArrayList<String> sqlUpdaters = new ArrayList<>();

        connect();
        try{

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,username);
            ResultSet rs = pstmt.executeQuery();

            //alle info in 1 x uit de rs halen, zodat we kunnen sluiten
            //als we de connectie sluiten voordat de info uit rs halen, krijgen we error:
            //java.sql.SQLException: The prepared statement has been finalized
            int dbWins=-1;
            int draws=-1;
            int dbLosses=-1;
            int eindScoreInDb4x4=-1;
            int eindScoreInDb6x6=-1;
            int aantalGames=-1;
            while(rs.next()) {
                dbWins = rs.getInt("wins");
                draws = rs.getInt("draws");
                dbLosses = rs.getInt("losses");
                eindScoreInDb4x4 = rs.getInt("max4x4");
                eindScoreInDb6x6 = rs.getInt("max6x6");
                aantalGames = rs.getInt("aantalGames");
            }

            closeConnection();
            aantalGames++;
            sqlUpdaters.add("UPDATE Scores SET aantalGames = "+ aantalGames +" WHERE Username = ? ");

            //maxScores per rooster updooten indien nodig

            if(roosterSize == 4){

                if(eindScore > eindScoreInDb4x4 ){
                    sqlUpdaters.add("UPDATE Scores SET max4x4 = "+ eindScore +" WHERE Username = ? ");
                }

            }

            else if(roosterSize == 6){

                if(eindScore > eindScoreInDb6x6 ){
                    sqlUpdaters.add("UPDATE Scores SET max6x6 = "+ eindScore +" WHERE Username = ? ");
                }

            }



            switch(command){

                case "WIN":

                    dbWins++;
                    sqlUpdaters.add("UPDATE Scores SET wins = "+ dbWins +" WHERE Username = ? ");
                    //dbwins moet terug weg
                    //aantalGames moet weg
                    break;

                case "LOSS":


                    dbLosses++;
                    sqlUpdaters.add("UPDATE Scores SET losses = "+ dbLosses +" WHERE Username = ? ");
                    break;


                case "DRAW":


                    draws++;
                    sqlUpdaters.add("UPDATE Scores SET draws = "+ draws +" WHERE Username = ? ");
                    break;


                default:
                    System.out.println("probleem in updateScores in databaseImpl");
                    break;

            }



            //voer elke sql string uit
            for (String sqlString : sqlUpdaters) {
                connect();
                PreparedStatement pstmttemp = conn.prepareStatement(sqlString);
                pstmttemp.setString(1, username);
                pstmttemp.executeUpdate();
                pstmttemp.close();
                closeConnection();
            }



            if(onMaster){
                for (DatabaseInterface dbRef : DataServerMain.pollToOtherDBs.getDBRefs()) {

                    new Thread(() -> {
                        try {
                            dbRef.updateScores(username, roosterSize, eindScore,command,false);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }).start();

                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean ping() throws RemoteException {
        return true;
    }

}
