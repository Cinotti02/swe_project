package ORM;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

public final class DatabaseMigrator {

    private DatabaseMigrator() {
    }

    public static void main(String[] args) throws Exception {
        Path migration = args.length > 0
                ? Path.of(args[0])
                : Path.of("sql", "migrations", "V2__prevent_double_table_booking.sql");

        String sql = Files.readString(migration);
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }

        System.out.println("Migrazione applicata: " + migration.toAbsolutePath());
    }
}
