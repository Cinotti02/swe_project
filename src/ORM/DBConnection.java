package ORM;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {
    private static final String PROPERTIES_FILE = "src/ORM/db.properties";
    private static String url;
    private static String user;
    private static String password;
    private static boolean initialized;

    //Costruttore privato
    private DBConnection(){
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (!initialized) {
            loadProperties();
            initialized = true;
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(PROPERTIES_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load database configuration", e);
        }
        url = readConfig("DB_URL", "db.URL", props);
        user = readConfig("DB_USER", "db.USER", props);
        password = readConfig("DB_PASSWORD", "db.PASSWORD", props);

        if (url == null || user == null || password == null) {
            throw new IllegalStateException("Database configuration is incomplete");
        }
    }

    // Ritorna la connessione unica
    private static String readConfig(String envKey, String propKey, Properties props) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        String propertyValue = props.getProperty(propKey);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }
        return null;
    }
}