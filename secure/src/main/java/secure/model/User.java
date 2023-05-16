package secure.model;

import java.sql.SQLException;

import secure.Database;

public class User {
    public final int id;
    public boolean isVendor;
    public final String username;
    public final String password;

    public User(int id, String username, String password, int isVendor) {
        this.id = id;
        this.isVendor = isVendor == 1;
        this.username = username;
        this.password = password;
    }

    public static User create(Database db, String username, String password, boolean isVendor) throws SQLException {
        var connection = db.getConnection();
        var sql = "INSERT INTO user (username, password, is_vendor) VALUES (?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setInt(3, isVendor ? 1 : 0);
            statement.execute();
            var keys = statement.getGeneratedKeys();
            int id = -1;
            if (keys.next()) {
                id = keys.getInt(1);
            } else {
                throw new RuntimeException("No key returned from INSERT INTO user");
            }
            connection.commit();
            return new User(id, username, password, isVendor ? 1 : 0);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public static User getUser(Database db, String username, String password) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT id, username, password, is_vendor FROM user WHERE username = ? AND password = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            var results = statement.executeQuery();
            if (results.next()) {
                return new User(results.getInt("id"), results.getString("username"), results.getString("password"),
                        results.getInt("is_vendor"));
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
        var sql = "SELECT id, username, password, is_vendor FROM user WHERE id = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            var results = statement.executeQuery();
            if (results.next()) {
                return new User(results.getInt("id"), results.getString("username"), results.getString("password"),
                        results.getInt("is_vendor"));
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
