package dk.kb.bitrepository.mediator.database;

public class DatabaseDAO {
    private final DatabaseConnectionManager manager;

    public DatabaseDAO(DatabaseConnectionManager manager) {
        this.manager = manager;
    }
}
