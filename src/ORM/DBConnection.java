package ORM;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static DBConnection instance;
    private Connection connection;
    private static final String propertiesFile = "src/ORM/db.properties";
    private static String url;
    private static String user;
    private static String password;

    //Costruttore privato
    private DBConnection() throws SQLException {
        loadProperties();
        this.connection = DriverManager.getConnection(url, user, password);
    }

    // Carica configurazione
    private void loadProperties() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(propertiesFile)) {
            props.load(fis);
            url = props.getProperty("db.URL");
            user = props.getProperty("db.USER");
            password = props.getProperty("db.PASSWORD");
        } catch (IOException e) {
            System.err.println("Error loading database configuration: " + e.getMessage());
            throw new RuntimeException("Unable to load database configuration", e);
        }
    }

    // Metodo Singleton — thread-safe
    public static synchronized DBConnection getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DBConnection();
        }
        return instance;
    }

    // Ritorna la connessione unica
    public Connection getConnection() {
        return connection;
    }
}