package dataserver;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataServerMain {



        /**
         * Connect to the test.db database
         *
         * @return the Connection object
         */
        private Connection connect() {
            // SQLite connection string
            String url = "jdbc:sqlite:D:\\School\\Ind Ing\\iiw Master\\Semester 1\\Gedistribueerde Systemen\\DS_Project\\data\\memorydb.db";
            Connection conn = null;
            try {
                conn = DriverManager.getConnection(url);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return conn;
        }

        /**
         * Insert a new row into the users table
         *
         * @param name
         * @param password
         */
        public void insertUser(String name, String password) {
            String sql = "INSERT INTO Persons(Username,Password) VALUES(?,?)";

            try (Connection conn = this.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, Hashing.sha256()
                        .hashString(password, StandardCharsets.UTF_8)
                        .toString());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }


        public boolean checkUser(String name, String password){
            String sql = "SELECT Password FROM Persons WHERE Username=?";

            try (

            Connection conn = this.connect();
            PreparedStatement pstmt  = conn.prepareStatement(sql)){

            pstmt.setString(1, name);

            ResultSet rs  = pstmt.executeQuery();
                    String retrievePassword="";
                    // loop through the result set
                    while (rs.next()) {
                        retrievePassword = rs.getString("Password");
                    }

                    if (retrievePassword.equals(Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString())) {
                        return true;
                    } else {
                        return false;
                    }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        public boolean userNameExists(String name){
            String sql = "SELECT Username FROM Persons WHERE Username=?";

            try (

                Connection conn = this.connect();
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




}
