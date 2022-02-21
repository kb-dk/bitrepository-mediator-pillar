package dk.kb.bitrepository.mediator.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dk.kb.bitrepository.mediator.utils.configurations.DatabaseConfigurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Simple database connection manager that provides database connections through a connection pool.
 */
public class DatabaseConnectionManager {
    private final static Logger log = LoggerFactory.getLogger(DatabaseConnectionManager.class);
    private DataSource pool;

    public DatabaseConnectionManager(DatabaseConfigurations dbConfig) {
        initializeConnectionPool(dbConfig);
    }

    /**
     * Initializes and configures the connection pool.
     * @param dbConfig The database specific configurations to use for the pool.
     */
    private void initializeConnectionPool(DatabaseConfigurations dbConfig) {
        log.debug("Initializing jdbc connection pool");
        HikariConfig poolConfig = new HikariConfig();
        poolConfig.setJdbcUrl(dbConfig.getUrl());
        poolConfig.setUsername(dbConfig.getUsername());
        poolConfig.setPassword(dbConfig.getPassword());
        // TODO configure connection pool more and silence logs(?)
        pool = new HikariDataSource(poolConfig);
    }

    /**
     * Grab a new connection from the pool.
     * If the 'maximumPoolSize' limit has been reached, this call will block for the configurable
     * 'connectionTimeout' milliseconds before timing out and throwing an exception.
     *
     * @return A new Connection to the database specified by the config provided through the constructor.
     * @throws SQLException If a database access error occurs.
     */
    public Connection getConnection() throws SQLException {
        return pool.getConnection();
    }


}
