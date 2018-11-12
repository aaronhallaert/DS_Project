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
        connect();
    }

    /**
     * Connect to the test.db database
     *
     */
    public void connect() {
        // SQLite connection string
        //String url = "jdbc:sqlite:D:\\School\\Ind Ing\\iiw Master\\Semester 1\\Gedistribueerde Systemen\\DS_Project\\data\\memorydb.db";
        //String workingDir = System.getProperty("user.dir");
        //String url = "jdbc:sqlite:"+workingDir+"\\DatabaseServer\\data\\memorydb.db";
        String url = "jdbc:sqlite:C:\\Users\\tibor\\JavaProjects\\DS_Project\\DS_Project\\DatabaseServer\\data\\memorydb.db";
        try {
            if(conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(url);
                //System.out.println("connection opened");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("error in connect() methode");
        }
    }

    /**
     * close the connection to the test.db database
     */
    public void closeConnection() {

        try {
            conn.close();
            //System.out.println("connection closed");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in closeconnection methode()");
        }
    }



    //METHODEN VOOR DE PERSONS TABLE
    @Override
    public boolean checkUserCred(String naam, String paswoord) throws RemoteException {

        String sql = "SELECT Password, Salt FROM Persons WHERE Username=?";

        //fire up the connection
        connect();

        boolean result = false;

        try (



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

            if (retrievePassword.equals(hash(paswoord, retrieveSalt))) {
                result = true;
            } else {
                result = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("sqlexception in checkUserCred");
        }
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
            System.out.println("problem in insertUser database");
        }
    }

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
        return false;
    }

    @Override
    public String createToken(String username, String password)throws RemoteException {
        if(checkUserCred(username, password)){
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

            return token;
        }
        else return null;
    }

    @Override
    public boolean isTokenValid(String username, String token) throws RemoteException{
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
            return false;
        }
        catch(SQLException se){
            return false;
        }
    }

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

            se.printStackTrace();
            return null;
        }
    }

    private static String hash(String password, String salt){
        return Hashing.sha256().hashString((password + salt),StandardCharsets.UTF_8).toString();
    }

    private static String hash(String password){
        return Hashing.sha256().hashString((password),StandardCharsets.UTF_8).toString();
    }

    //METHODEN VOOR DE PICTURES TABLE

    @Override /* METHODE NOG NIET OKé */
    public byte[] getImage(String naam) throws RemoteException { //in 0 zit de achterkant van de foto

        String sql = "SELECT image FROM pictures WHERE naam=?";

        byte[] array = null;

        connect();

        try {

            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, naam);

            ResultSet rs = pstmt.executeQuery();

            array = rs.getBytes("image");

            rs.close();
            pstmt.close();



        }

        catch (SQLException e) {
            System.out.println("exception in databaseImpl alweer");
            e.printStackTrace();
        }

        closeConnection();

        return array;
    }

    @Override
    public void storeImage(String naamFoto, byte[] afbeelding) throws RemoteException{

        System.out.println("database storen van een image started");

        String sql = "INSERT INTO pictures(naam,image) VALUES(?,?)";

        connect();

        try {


            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, naamFoto);
            pstmt.setBytes(2, afbeelding);
            pstmt.executeUpdate();

            pstmt.close();
            conn.close();

        } catch (SQLException e) {
            System.out.println("fout in converteren van de afbeelding");
            e.printStackTrace();
            System.out.println("fout in converteren van de afbeelding");
        }

    }


}
