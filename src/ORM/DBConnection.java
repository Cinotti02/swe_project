package ORM;

import org.postgresql.ds.PGPoolingDataSource;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {
    private static final String PROPERTIES_FILE = "src/ORM/db.properties";
    private static final String CLASSPATH_PROPERTIES_FILE = "ORM/db.properties";
    private final DataSource dataSource;
    private final String schema;

    private DBConnection() {
        Properties props = loadProperties();

        String url = normalizeUrl(readConfig("db.url", "DB_URL", "db.URL", props));
        String user = readConfig("db.user", "DB_USER", "db.USER", props);
        String password = readConfig("db.password", "DB_PASSWORD", "db.PASSWORD", props);

        if (url == null || user == null || password == null) {
            throw new IllegalStateException("Database configuration is incomplete");
        }

        this.schema = readCurrentSchema(url);

        PGPoolingDataSource ds = new PGPoolingDataSource();
        ds.setDataSourceName("dineup-pool");
        ds.setUrl(url);
        ds.setUser(user);
        ds.setPassword(password);
        ds.setCurrentSchema(schema);
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
        DBConnection instance = getInstance();
        Connection connection = instance.dataSource.getConnection();
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT set_config('search_path', ?, false)")) {
            statement.setString(1, instance.schema);
            statement.execute();
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
        return connection;
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

    private static String readConfig(String systemKey,
                                     String envKey,
                                     String propKey,
                                     Properties props) {
        String systemValue = System.getProperty(systemKey);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }

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

    private static String normalizeUrl(String url) {
        if (url == null || url.toLowerCase().contains("currentschema=")) {
            return url;
        }
        return url + (url.contains("?") ? "&" : "?") + "currentSchema=public";
    }

    private static String readCurrentSchema(String url) {
        if (url == null) {
            return "public";
        }
        String lower = url.toLowerCase();
        int start = lower.indexOf("currentschema=");
        if (start < 0) {
            return "public";
        }
        start += "currentschema=".length();
        int end = url.indexOf('&', start);
        return url.substring(start, end >= 0 ? end : url.length());
    }
}
