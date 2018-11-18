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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public void pushGames(ArrayList<Game> games) throws RemoteException{
        // vraag alle game id's op uit db
        Set<Integer> gameIdList= new HashSet<>();
        connect();
        String getGameId= "SELECT gameId FROM GameInfo";
        try {
            PreparedStatement pstmt= conn.prepareStatement(getGameId);
            ResultSet rs= pstmt.executeQuery();
            while(rs.next()){
                gameIdList.add(rs.getInt("gameId"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (Game game : games) {
            if(!gameIdList.contains(game.getGameId())) {
                // push game state
                pushGameState(game);

                // push tegellijst
                pushTegellijst(game, game.getGameState().getTegelsList().size());

                // push Game Info
                pushGameInfo(game);
            }
            else{
                //update game nodig
                updateGameState(game);

                updateGameInfo(game);

                updateTegelLijst(game);
            }

        }

        closeConnection();
    }
    private void pushGameInfo(Game game){
        String pushGameInfo= "INSERT INTO GameInfo(gameId, clientA, clientB, aantalSpelersConnected, fotoSet, roosterSize) " +
                "VALUES (?,?,?,?,?,?)";

        try {
            PreparedStatement pstmtGameInfo= conn.prepareStatement(pushGameInfo);

            pstmtGameInfo.setInt(1,game.getGameId());
            pstmtGameInfo.setString(2, game.getGameInfo().getClientA());
            pstmtGameInfo.setString(3, game.getGameInfo().getClientB());
            pstmtGameInfo.setInt(4,game.getGameInfo().getAantalSpelersConnected());
            pstmtGameInfo.setString(5, game.getGameInfo().getFotoSet());
            pstmtGameInfo.setInt(6,game.getGameInfo().getRoosterSize());

            pstmtGameInfo.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void pushGameState(Game game){
        // push game state
        String pushGameState = "INSERT INTO GameState(gameId, aantalParen, aantalPerRij, naamSpelerA, naamSpelerB, " +
                "aantalPuntenSpelerA, aantalPuntenSpelerB, aandeBeurt, tileListSize, aantalParenFound, finished) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        connect();
        try {
            PreparedStatement pstmtGameState=conn.prepareStatement(pushGameState);

            pstmtGameState.setInt(1, game.getGameId());
            pstmtGameState.setInt(2, game.getGameState().getAantalParen());
            pstmtGameState.setInt(3, game.getGameState().getAantalPerRij());
            pstmtGameState.setString(4, game.getGameState().getNaamSpelerA());
            pstmtGameState.setString(5, game.getGameState().getNaamSpelerB());
            pstmtGameState.setInt(6, game.getGameState().getAantalPuntenSpelerA());
            pstmtGameState.setInt(7, game.getGameState().getAantalPuntenSpelerB());
            pstmtGameState.setString(8, Character.toString(game.getGameState().getAandeBeurt()));
            pstmtGameState.setInt(9, game.getGameState().getTegelsList().size());
            pstmtGameState.setInt(10, game.getGameState().getAantalParenFound());
            pstmtGameState.setBoolean(11, game.getGameState().getfinished());
            pstmtGameState.executeUpdate();



        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void pushTegellijst(Game game, int size){
        StringBuilder sb= new StringBuilder();
        if (size == 16) {
            sb.append("INSERT INTO TileList4x4(gameId");
        }
        else if (size==36){
            sb.append("INSERT INTO TileList6x6(gameId");
        }

        for (int i = 1; i < size+1; i++) {
            sb.append(", Tile").append(i);
        }
        sb.append(") VALUES(?");
        for (int i = 0; i < size; i++) {
            sb.append(",?");
        }
        sb.append(")");

        String pushList= sb.toString();
        try {
            PreparedStatement pstmtTegelLijst = conn.prepareStatement(pushList);
            pstmtTegelLijst.setInt(1, game.getGameId());

            // push elke tegel
            int i=2;
            for (Tile tile : game.getGameState().getTegelsList()) {
                String pushTegel = "INSERT INTO Tile(uniqueIdentifier, id, imageId, backImageId, isfound, flippedOver, gameId)" +
                        "VALUES(?,?,?,?,?,?,?)";


                PreparedStatement pstmtTegel = conn.prepareStatement(pushTegel);

                pstmtTegel.setInt(1, tile.getUniqueIdentifier());
                pstmtTegel.setInt(2, tile.getId());
                pstmtTegel.setString(3, tile.getImageId());
                pstmtTegel.setString(4, tile.getBackImageId());
                pstmtTegel.setBoolean(5, tile.isFound());
                pstmtTegel.setBoolean(6, tile.isFlippedOver());
                pstmtTegel.setInt(7, game.getGameId());

                pstmtTegel.executeUpdate();

                pstmtTegelLijst.setInt(i,tile.getUniqueIdentifier());
                i++;



            }

            pstmtTegelLijst.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void updateGameState(Game game){
        String updateGameString= "UPDATE GameState " +
                "SET aantalPuntenSpelerA = ?, aantalPuntenSpelerB = ?, aandeBeurt= ?, aantalParenFound= ?" +
                "WHERE gameId = ?";
        try {
            PreparedStatement pstmtGameState= conn.prepareStatement(updateGameString);
            pstmtGameState.setInt(1,game.getGameState().getAantalPuntenSpelerA());
            pstmtGameState.setInt(2,game.getGameState().getAantalPuntenSpelerB());
            pstmtGameState.setString(3, Character.toString(game.getGameState().getAandeBeurt()));
            pstmtGameState.setInt(4, game.getGameState().getAantalParenFound());
            pstmtGameState.setInt(5, game.getGameId());


            pstmtGameState.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void updateGameInfo(Game game){
        String updateGameInfo= "UPDATE GameInfo " +
                "SET clientA = ?, clientB = ?, aantalSpelersConnected= ?" +
                "WHERE gameId = ?";
        try {
            PreparedStatement pstmtGameState= conn.prepareStatement(updateGameInfo);
            pstmtGameState.setString(1,game.getGameInfo().getClientA());
            pstmtGameState.setString(2,game.getGameInfo().getClientB());
            pstmtGameState.setInt(3, game.getGameInfo().getAantalSpelersConnected());
            pstmtGameState.setInt(4, game.getGameId());



            pstmtGameState.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTegelLijst(Game game){

        for (Tile tile : game.getGameState().getTegelsList()) {
            String updateTile= "UPDATE Tile " +
                    "SET flippedOver = ?, isfound = ?"+
                    "WHERE gameId= ? AND uniqueIdentifier = ?";

            try {
                PreparedStatement pstmtTile= conn.prepareStatement(updateTile);
                pstmtTile.setBoolean(1, tile.isFound());
                pstmtTile.setBoolean(2, tile.isFlippedOver());
                pstmtTile.setInt(3, game.getGameId());
                pstmtTile.setInt(4, tile.getUniqueIdentifier());

                pstmtTile.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
                int aantalParenFound= rs.getInt("aantalParenFound");

                char aandeBeurt= rs.getString("aandeBeurt").charAt(0);



                int tileListSize= rs.getInt("tileListSize");

                ArrayList<Tile> tiles=  getTiles(gameId, tileListSize);
                GameState gs =new GameState(gameId, aantalParen, aantalPerRij, naamSpelerA, naamSpelerB, aantalPuntenSpelerA, aantalPuntenSpelerB, aandeBeurt, tiles, aantalParenFound);
                gameStates.add(gs);
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
        closeConnection();

        return null;
    }

    private ArrayList<Tile> getTiles(int gameId, int tileListSize) throws SQLException {
        ArrayList<Tile> tiles= new ArrayList<>();
        String sqlTiles="";
        if(tileListSize==16) {
            sqlTiles = "SELECT * FROM TileList4x4 WHERE gameId = ? ";
        }
        else if(tileListSize==36) {
            sqlTiles="SELECT * FROM TileList6x6 WHERE gameId = ?";
        }
        PreparedStatement tilepstmt= conn.prepareStatement(sqlTiles);
        tilepstmt.setInt(1, gameId);
        ResultSet tilers= tilepstmt.executeQuery();
        ArrayList<Integer> tileIds= new ArrayList<>();
        while(tilers.next()){
            for (int i = 1; i < tileListSize+1; i++) {
                String tile= "Tile"+i;
                tileIds.add(tilers.getInt(tile));
            }
        }

        for (Integer tileId : tileIds) {
            String sqlTile= "SELECT * FROM TILE WHERE uniqueIdentifier = ? and gameId= ?";
            PreparedStatement singleTilepstmt= conn.prepareStatement(sqlTile);
            singleTilepstmt.setInt(1, tileId);
            singleTilepstmt.setInt(2, gameId);
            ResultSet tile= singleTilepstmt.executeQuery();

            while(tile.next()){
                int uniqueIdentifier=tile.getInt("uniqueIdentifier");
                int id= tile.getInt("id");
                String imageId= tile.getString("imageId");
                String backImageId= tile.getString("backImageId");
                boolean found= tile.getBoolean("isfound");
                boolean flippedOver= tile.getBoolean("flippedOver");
                tiles.add(new Tile(uniqueIdentifier, id, imageId, backImageId, found, flippedOver));
            }

        }

        return tiles;


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
