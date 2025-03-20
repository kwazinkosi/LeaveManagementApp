package infrastructure.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class SchemaManager {
    private static final Logger LOGGER = Logger.getLogger(SchemaManager.class.getName());
    private static final String SCHEMA_FILE = "/schema.sql";

    public static void initializeSchema() {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            
            String schemaSql = loadSchemaFile();
            executeSchemaStatements(stmt, schemaSql);
            LOGGER.info("Database schema initialized successfully");
            
        } catch (SQLException | IOException e) {
            LOGGER.severe("Schema initialization failed: " + e.getMessage());
            throw new RuntimeException("Database schema initialization failed", e);
        }
    }

    private static String loadSchemaFile() throws IOException {
        try (InputStream inputStream = SchemaManager.class.getResourceAsStream(SCHEMA_FILE)) {
            if (inputStream == null) {
                throw new IOException("Schema file not found: " + SCHEMA_FILE);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void executeSchemaStatements(Statement stmt, String schemaSql) throws SQLException {
        // Split SQL script into individual statements
        String[] statements = schemaSql.split(";\\s*\n");
        
        for (String sql : statements) {
            String trimmed = sql.trim();
            if (!trimmed.isEmpty()) {
                try {
                    stmt.execute(trimmed);
                } catch (SQLException e) {
                    LOGGER.warning("Failed to execute statement: " + trimmed);
                    throw e;
                }
            }
        }
    }
}