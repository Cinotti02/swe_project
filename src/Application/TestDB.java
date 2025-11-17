package Application;

import ORM.DBConnection;
import java.sql.Connection;

public class TestDB {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Connessione OK → " + conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}