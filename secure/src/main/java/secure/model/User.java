package secure.model;

import java.sql.SQLException;

import secure.*;

public class User {
    public final String id;
    public boolean isVendor;
    public final String username;
    public final String password;

    public User(String id, String username, String password, int isVendor) {
        this.id = id;
        this.isVendor = isVendor == 1;
        this.username = username;
        this.password = password;
    }

    public static User create(Database db, String username, String password, boolean isVendor) throws SQLException {
        var connection = db.getConnection();
        var sql = "INSERT INTO user (id, username, password, is_vendor, salt) VALUES (?, ?, ?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            var uuid = Utils.newUuid();
            var salt = Random.instance().nextBytesBase64(64);
            var passwordHash = Utils.hash(password, salt);
            statement.setString(1, uuid);
            statement.setString(2, username);
            statement.setString(3, passwordHash);
            statement.setInt(4, isVendor ? 1 : 0);
            statement.setString(5, salt);
            statement.execute();
            connection.commit();
            return new User(uuid, username, password, isVendor ? 1 : 0);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public static User getUser(Database db, String username, String password) throws Exception {
        var connection = db.getConnection();
        var sql = "SELECT id, username, password, salt, is_vendor FROM user WHERE username = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            var results = statement.executeQuery();
            if (results.next()) {

                var salt = results.getString("salt");
                var hashedPassword = Utils.hash(password, salt);

                if (!results.getString("password").equals(hashedPassword)) {
                    Thread.sleep(1000);
                    return null;
                }

                return new User(results.getString("id"), results.getString("username"), results.getString("password"),
                        results.getInt("is_vendor"));
            } else {
                Thread.sleep(1000);
                return null;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            connection.rollback();
        }
    }

    public static User getUser(Database db, String id) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT id, username, password, is_vendor FROM user WHERE id = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            var results = statement.executeQuery();
            if (results.next()) {
                return new User(results.getString("id"), results.getString("username"), results.getString("password"),
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
