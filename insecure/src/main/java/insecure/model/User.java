package insecure.model;

import java.sql.SQLException;

import insecure.Database;

public class User {
    public final int id;
    public final String username;
    public final String password;

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public static User create(Database db, String username, String password) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            statement.execute("INSERT INTO user (username, password) VALUES ('" + username + "', '" + password + "')");
            var keys = statement.getGeneratedKeys();
            int id = -1;
            if (keys.next()) {
                id = keys.getInt(1);
            } else {
                throw new RuntimeException("No key returned from INSERT INTO user");
            }
            connection.commit();
            return new User(id, username, password);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public static User getUser(Database db, String username, String password) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            var results = statement.executeQuery("SELECT id, username, password FROM user WHERE username = '" + username
                    + "' AND password = '" + password + "'");
            if (results.next()) {
                return new User(results.getInt(1), results.getString(2), results.getString(3));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            connection.rollback();
        }
    }

    public static User getUser(Database db, int id) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            var results = statement.executeQuery("SELECT id, username, password FROM user WHERE id = " + id);
            if (results.next()) {
                return new User(results.getInt(1), results.getString(2), results.getString(3));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            connection.rollback();
        }
    }
}
