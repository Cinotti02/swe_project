package ORM;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseDAO {

    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    // Metodi comuni a tutti i DAO (estendibili in futuro)
    protected void checkId(int id) {
        if (id <= 0)
            throw new IllegalArgumentException("ID must be positive");
    }
}