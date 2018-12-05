import Classes.Game;
import Classes.Score;
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

    private String databaseNaam;
    private static Connection conn = null;
    private ArrayList<DatabaseInterface> otherDbs=new ArrayList<>();


    public DatabaseImpl(String databaseNaam) throws RemoteException{

        this.databaseNaam = databaseNaam;
        // maakt connectie met sql database
        connect();
    }


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



    //METHODEN VOOR DE PERSONS TABLE

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
     * Insert a new row into the users table
     *
     * @param name
     * @param password
     */
    @Override
    public void insertUser(String name, String password) {
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
    }


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
     * aanmaken van token (hash van paswoord en de huidige tijd in ms)
     * @param username
     * @param password
     * @throws RemoteException
     */
    @Override
    public void createToken(String username, String password)throws RemoteException {

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
    public void cancelToken(String username) throws RemoteException{
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


    @Override
    public void connectTo(DatabaseInterface toImpl) throws RemoteException {
        otherDbs.add(toImpl);
    }




    private static String hash(String password, String salt){
        return Hashing.sha256().hashString((password + salt),StandardCharsets.UTF_8).toString();
    }


    private static String hash(String password){
        return Hashing.sha256().hashString((password),StandardCharsets.UTF_8).toString();
    }




    //METHODEN VOOR DE PICTURES TABLE


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
    public void storeImage(String afbeeldingId, byte[] afbeelding) throws RemoteException{

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

    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ SCORETABEL @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

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





        } catch (SQLException e) {
            e.printStackTrace();
        }


        return scoreList;

    }

    @Override
    public void insertScoreRow(String username) throws RemoteException {

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

    }

    @Override
    public void updateScores(String username, int roosterSize, int eindScore, String command) throws RemoteException {

        String sql = "SELECT * FROM Scores WHERE Username = ? ";

        ArrayList<String> sqlUpdaters = new ArrayList<>();

        connect();
        try{

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,username);
            ResultSet rs = pstmt.executeQuery();

            int aantalGames = rs.getInt("aantalGames");
            aantalGames++;
            sqlUpdaters.add("UPDATE Scores SET aantalGames = "+ aantalGames +" WHERE Username = ? ");

            //maxScores per rooster updooten indien nodig

            if(roosterSize == 4){

                if(eindScore > rs.getInt("max4x4") ){
                    sqlUpdaters.add("UPDATE Scores SET max4x4 = "+ eindScore +" WHERE Username = ? ");
                }

            }

            else if(roosterSize == 6){

                if(eindScore > rs.getInt("max6x6") ){
                    sqlUpdaters.add("UPDATE Scores SET max6x6 = "+ eindScore +" WHERE Username = ? ");
                }

            }



            switch(command){

                case "WIN":

                    int dbWins = rs.getInt("wins");
                    dbWins++;
                    sqlUpdaters.add("UPDATE Scores SET wins = "+ dbWins +" WHERE Username = ? ");
                    //dbwins moet terug weg
                    //aantalGames moet weg
                    break;

                case "LOSS":

                    int dbLosses = rs.getInt("losses");
                    dbLosses++;
                    sqlUpdaters.add("UPDATE Scores SET wins = "+ dbLosses +" WHERE Username = ? ");
                    break;


                case "DRAW":

                    int draws = rs.getInt("draws");
                    draws++;
                    sqlUpdaters.add("UPDATE Scores SET wins = "+ draws +" WHERE Username = ? ");
                    break;


                default:
                    System.out.println("probleem in updateScores in databaseImpl");
                    break;

            }


            //voer elke sql string uit
            for (String sqlString : sqlUpdaters) {

                PreparedStatement pstmttemp = conn.prepareStatement(sqlString);
                pstmttemp.setString(1, username);
                pstmttemp.executeUpdate();
                pstmttemp.close();
            }

            closeConnection();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


}
