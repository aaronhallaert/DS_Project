package dataserver;


import interfaces.DatabaseInterface;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseImpl extends UnicastRemoteObject implements DatabaseInterface {


    Connection conn;
    public DatabaseImpl() throws RemoteException{
        conn= connect();
    }


    @Override
    public boolean checkUserCred(String naam, String paswoord) throws RemoteException {
        String sql = "SELECT Password, Salt FROM Persons WHERE Username=?";

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
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
        try (Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, hashedPaswoord);
            pstmt.setString(3, salt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public boolean userNameExists(String name){
        String sql = "SELECT Username FROM Persons WHERE Username=?";

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
        }
        return false;
    }

    @Override
    public String createToken(String username, String password)throws RemoteException {
        if(checkUserCred(username, password)){
            String sql="UPDATE Persons SET token=?, token_timestamp=? WHERE Username= ?;";
            String token= hash(password+System.currentTimeMillis());

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


    /**
     * Connect to the test.db database
     *
     * @return the Connection object
     */
    public Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:D:\\School\\Ind Ing\\iiw Master\\Semester 1\\Gedistribueerde Systemen\\DS_Project\\data\\memorydb.db";
        //String url = "jdbc:sqlite:C:\\Users\\tibor\\JavaProjects\\DS_Project\\DS_Project\\data\\memorydb.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    private static String hash(String password, String salt){
        return Hashing.sha256().hashString((password + salt),StandardCharsets.UTF_8).toString();
    }

    private static String hash(String password){
        return Hashing.sha256().hashString((password),StandardCharsets.UTF_8).toString();
    }
}
