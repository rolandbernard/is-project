package secure.model;

import java.sql.SQLException;
import java.util.Arrays;

import secure.*;
import secure.Rsa.*;

public class User {
    public final String id;
    public boolean isVendor;
    public final String username;
    public RsaKey publicKey = null;
    public RsaKey privateKey = null;

    public User(String id, String username, int isVendor, RsaKey publicKey, RsaKey privateKey) {
        this.id = id;
        this.isVendor = isVendor == 1;
        this.username = username;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public User(String id, String username, int isVendor, RsaKey publicKey) {
        this(id, username, isVendor, publicKey, null);
    }

    public static User create(Database db, String username, String password, boolean isVendor) throws SQLException {
        var connection = db.getConnection();
        var sql = "INSERT INTO user (id, username, password, is_vendor, salt, public_key, private_key, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            var uuid = Utils.newUuid();
            var salt = Random.instance().nextBytes(64);
            var keys = Rsa.generateKeys(2048);
            var publicKey = keys.pub().toByteArray();
            var desKey = Hash.keyDerivation(password, salt, "private key encryption", 16);
            var privateKey = Des.encryptCbc(keys.priv().toByteArray(), desKey);
            var passwordHash = Hash.keyDerivation(password, salt, "password hash", 32);
            var timestamp = System.currentTimeMillis();
            statement.setString(1, uuid);
            statement.setString(2, username);
            statement.setBytes(3, passwordHash);
            statement.setInt(4, isVendor ? 1 : 0);
            statement.setBytes(5, salt);
            statement.setBytes(6, publicKey);
            statement.setBytes(7, privateKey);
            statement.setLong(8, timestamp);
            statement.execute();
            connection.commit();
            return new User(uuid, username, isVendor ? 1 : 0, keys.pub(), keys.priv());
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public static User getUser(Database db, String username, String password) throws Exception {
        var connection = db.getConnection();
        var sql = "SELECT id, username, password, salt, is_vendor, private_key, public_key FROM user WHERE username = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            var results = statement.executeQuery();
            if (results.next()) {
                var salt = results.getBytes("salt");
                var passwordHash = Hash.keyDerivation(password, salt, "password hash", 32);
                if (!Arrays.equals(passwordHash, results.getBytes("password"))) {
                    Thread.sleep(1000);
                    return null;
                }
                var publicKey = RsaKey.fromByteArray(results.getBytes("public_key"));
                var desKey = Hash.keyDerivation(password, salt, "private key encryption", 16);
                var privKeyBytes = Des.decryptCbc(results.getBytes("private_key"), desKey);
                var privateKey = RsaKey.fromByteArray(privKeyBytes);
                return new User(results.getString("id"), results.getString("username"),
                        results.getInt("is_vendor"), publicKey, privateKey);
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
        var sql = "SELECT id, username, is_vendor, public_key FROM user WHERE id = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            var results = statement.executeQuery();
            if (results.next()) {
                var publicKey = RsaKey.fromByteArray(results.getBytes("public_key"));
                return new User(results.getString("id"), results.getString("username"),
                        results.getInt("is_vendor"), publicKey);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            connection.rollback();
        }
    }

    public static boolean isUsernameFree(Database db, String username) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT id FROM user WHERE username = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            var results = statement.executeQuery();
            if (results.next() && results.getString("id") != null) {
                return false;
            } else {
                return true;
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            connection.rollback();
        }
    }
}
