package dataserver;

import appserver.AppServiceImpl;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataServerMain {

    public static void main(String[] args) {

        //vast poortnummer
        try {
            System.out.println("dataserver gelaunched op: "+ args[0]);
            Registry dataRegistry= LocateRegistry.createRegistry(Integer.parseInt(args[0]));
            dataRegistry.rebind("DatabaseService", new DatabaseImpl());

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }













}
