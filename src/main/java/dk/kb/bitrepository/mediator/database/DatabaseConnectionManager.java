package dk.kb.bitrepository.mediator.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dk.kb.bitrepository.mediator.MediatorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionManager {
    private final static Logger log = LoggerFactory.getLogger(DatabaseConnectionManager.class);
    private DataSource pool;

    public DatabaseConnectionManager(MediatorConfiguration pillarConfig) {
        initializeConnectionPool(pillarConfig);
    }

    private void initializeConnectionPool(MediatorConfiguration pillarConfig) {
        log.debug("Initializing jdbc connection pool");
        HikariConfig poolConfig = new HikariConfig();
        poolConfig.setJdbcUrl(pillarConfig.getDatabaseURL() + ";create=true");
        poolConfig.setUsername(pillarConfig.getDatabaseUsername());
        poolConfig.setPassword(pillarConfig.getDatabasePassword());
        // TODO configure connection pool more and silence logs(?)
        pool = new HikariDataSource(poolConfig);
    }

    public Connection getConnection() throws SQLException {
        return pool.getConnection();
    }


}
