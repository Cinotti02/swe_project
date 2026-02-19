package ORM;

import org.postgresql.ds.PGPoolingDataSource;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {
    private static final String PROPERTIES_FILE = "src/ORM/db.properties";
    private static final String CLASSPATH_PROPERTIES_FILE = "ORM/db.properties";
    private final DataSource dataSource;

    private DBConnection() {
        Properties props = loadProperties();

        String url = readConfig("DB_URL", "db.URL", props);
        String user = readConfig("DB_USER", "db.USER", props);
        String password = readConfig("DB_PASSWORD", "db.PASSWORD", props);

        if (url == null || user == null || password == null) {
            throw new IllegalStateException("Database configuration is incomplete");
        }

        PGPoolingDataSource ds = new PGPoolingDataSource();
        ds.setDataSourceName("dineup-pool");
        ds.setUrl(url);
        ds.setUser(user);
        ds.setPassword(password);
        ds.setInitialConnections(4);
        ds.setMaxConnections(20);
        this.dataSource = ds;
    }

    private static class Holder {
        private static final DBConnection INSTANCE = new DBConnection();
    }

    public static DBConnection getInstance() {
        return Holder.INSTANCE;
    }

    public static Connection getConnection() throws SQLException {
        return getInstance().dataSource.getConnection();
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        boolean loaded = false;

        try (InputStream classpathStream = DBConnection.class.getClassLoader().getResourceAsStream(CLASSPATH_PROPERTIES_FILE)) {
            if (classpathStream != null) {
                props.load(classpathStream);
                loaded = true;
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load database configuration from classpath", e);
        }
        if (!loaded) {
            try (FileInputStream fis = new FileInputStream(PROPERTIES_FILE)) {
                props.load(fis);
                loaded = true;
            } catch (IOException ignored) {
                // Nessun file locale: si tenta solo con variabili ambiente
            }
        }
        return props;
    }

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