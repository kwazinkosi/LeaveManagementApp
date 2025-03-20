package infrastructure.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class DatabaseConnectionManager {
   
	private static final Logger LOGGER = Logger.getLogger(DatabaseConnectionManager.class.getName());
    private static volatile DatabaseConnectionManager instance;

    private final String url;
    private final String username;
    private final String password;

    // Private constructor to enforce singleton
    private DatabaseConnectionManager(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        testConnection(); // Verify connection parameters on startup
    }

    // Improved thread-safe singleton instantiation
    public static DatabaseConnectionManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionManager.class) {
                if (instance == null) {
                    instance = createInstance();
                }
            }
        }
        return instance;
    }

    private static DatabaseConnectionManager createInstance() {
        try (InputStream input = DatabaseConnectionManager.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            
            if (input == null) {
                throw new RuntimeException("Config file not found in classpath: config.properties");
            }

            Properties props = new Properties();
            props.load(input);

            return new DatabaseConnectionManager(
                getRequiredProperty(props, "db.url"),
                getRequiredProperty(props, "db.user"),
                getRequiredProperty(props, "db.password")
            );
            
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration", e);
        }
    }

    private static String getRequiredProperty(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Missing required property: " + key);
        }
        return value;
    }

    private void testConnection() {
        
    	try (Connection conn = getConnection()) {
            
        	if (!conn.isValid(2)) {
                throw new SQLException("Connection validation failed");
            }
            
            LOGGER.info("Successfully connected to database");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish initial database connection", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}