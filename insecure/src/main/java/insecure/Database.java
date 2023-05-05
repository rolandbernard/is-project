package insecure;

import java.io.IOException;
import java.sql.*;

public final class Database implements AutoCloseable {
    static {
        try (var database = new Database()) {
            database.initializeSchema();
        } catch (SQLException | IOException e) {
            Utils.panic(e);
        }
    }

    private Connection connection;

    public Database() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:./database.db");
        connection.setAutoCommit(false);
    }

    private void initializeSchema() throws SQLException, IOException {
        var initScriptStream = Database.class.getResourceAsStream("init.sql");
        var initScript = new String(initScriptStream.readAllBytes(), "UTF8");
        var statement = connection.createStatement();
        for (var stmt : initScript.split(";")) {
            if (!stmt.strip().isEmpty()) {
                statement.execute(stmt);
            }
        }
        statement.close();
        connection.commit();
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.rollback();
            connection.close();
            connection = null;
        }
    }
}
