import Classes.Game;
import Classes.GameInfo;
import Classes.GameState;
import Classes.Tile;
import interfaces.DatabaseInterface;
import com.google.common.hash.Hashing;


import javax.sql.rowset.serial.SerialBlob;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseImpl extends UnicastRemoteObject implements DatabaseInterface {


    private static Connection conn = null;


    public DatabaseImpl() throws RemoteException{
        // maakt connectie met sql database
        connect();
    }


    /**
     * connectie met databank maken
     */
    public void connect() {

        // SQLite connection string
        String workingDir = System.getProperty("user.dir");
        String url = "jdbc:sqlite:"+workingDir+"\\DatabaseServer\\data\\memorydb.db";

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

        try{

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,"0");
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
        catch (SQLException se){
            se.printStackTrace();
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
            return rs.getString("token");


        }
        catch(SQLException se){
            System.out.println(se.getMessage());
            se.printStackTrace();
            return null;
        }
    }

    @Override
    public ArrayList<Game> getGames() throws RemoteException {

        ArrayList<Game> games= new ArrayList<>();
        ArrayList<GameInfo>gameInfos= new ArrayList<>();
        ArrayList<GameState> gameStates= new ArrayList<GameState>();

        String sqlGameInfo= "SELECT * FROM GameInfo";
        connect();
        try{
            PreparedStatement pstmt = conn.prepareStatement(sqlGameInfo);
            ResultSet rs= pstmt.executeQuery();
            while(rs.next()){
                int gameId = rs.getInt("gameId");
                String clientA= rs.getString("clientA");
                String clientB= rs.getString("clientB");
                int aantalSpelersConnected= rs.getInt("aantalSpelersConnected");
                String fotoSet= rs.getString("fotoSet");
                int roosterSize=rs.getInt("roosterSize");
                gameInfos.add(new GameInfo(gameId, clientA, clientB, aantalSpelersConnected, fotoSet, roosterSize));
            }

        }
        catch (SQLException e){
            e.printStackTrace();
        }

        String sqlGameState= "SELECT * FROM GameState";
        connect();
        try{
            PreparedStatement pstmt = conn.prepareStatement(sqlGameState);
            ResultSet rs= pstmt.executeQuery();
            while(rs.next()){
                int gameId= rs.getInt("gameId");
                int aantalParen= rs.getInt("aantalParen");
                int aantalPerRij= rs.getInt("aantalPerRij");
                String naamSpelerA= rs.getString("naamSpelerA");
                String naamSpelerB= rs.getString("naamSpelerB");
                int aantalPuntenSpelerA= rs.getInt("aantalPuntenSpelerA");
                int aantalPuntenSpelerB= rs.getInt("aantalPuntenSpelerB");
                char aandeBeurt= rs.getString("aandeBeurt").charAt(0);

                int tileListId= rs.getInt("tileListId");
                String tileListSize= rs.getString("tileListSize");

                ArrayList<Tile> tiles= new ArrayList<>();
                if(tileListSize.equals("4x4")){
                    String sqlTiles= "SELECT * FROM TileList4x4 WHERE TileListId = ?";
                    PreparedStatement tilepstmt= conn.prepareStatement(sqlTiles);
                    ResultSet tilers= tilepstmt.executeQuery();
                    ArrayList<Integer> tileIds= new ArrayList<>();
                    while(tilers.next()){
                        for (int i = 1; i < 17; i++) {
                            String tile= "Tile"+i;
                            tileIds.add(rs.getInt(tile));
                        }
                    }

                    for (Integer tileId : tileIds) {
                        String sqlTile= "SELECT * FROM TILE WHERE uniqueIdentifier = ?";
                        PreparedStatement singleTilepstmt= conn.prepareStatement(sqlTile);
                        ResultSet tile= singleTilepstmt.executeQuery();

                        while(tile.next()){
                            int uniqueIdentifier=rs.getInt("uniqueIdentifier");
                            int id= rs.getInt("id");
                            String imageId= rs.getString("imageId");
                            String backImageId= rs.getString("backImageId");
                            boolean found= rs.getBoolean("found");
                            boolean flippedOver= rs.getBoolean("flippedOver");
                            tiles.add(new Tile(uniqueIdentifier, id, imageId, backImageId, found, flippedOver));
                        }

                    }
                }

                gameStates.add(new GameState(gameId, aantalParen, aantalPerRij, naamSpelerA, naamSpelerB, aantalPuntenSpelerA, aantalPuntenSpelerB, aandeBeurt, tiles));
            }

            for (GameInfo gameInfo : gameInfos) {
                for (GameState gameState : gameStates) {
                    if(gameInfo.getGameId() == gameState.getGameId()){
                        games.add(new Game(gameInfo.getGameId(), gameInfo, gameState));
                    }
                }
            }

            return games;
        }
        catch (SQLException e){
            e.printStackTrace();
        }

        return null;
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


}
