package secure.model;

import java.sql.SQLException;
import java.util.Arrays;

import secure.*;
import secure.Rsa.*;
import secure.Dh.*;

public class User {
    public final String id;
    public final String username;
    public boolean isVendor;
    public RsaKey rsaPublicKey = null;
    public RsaKey rsaPrivateKey = null;
    public DhKey dhPublicKey = null;
    public DhKey dhPrivateKey = null;

    public User(String id, String username, int isVendor, RsaKey rsaPublicKey, RsaKey rsaPrivateKey, DhKey dhPublicKey,
            DhKey dhPrivateKey) {
        this.id = id;
        this.isVendor = isVendor == 1;
        this.username = username;
        this.rsaPublicKey = rsaPublicKey;
        this.rsaPrivateKey = rsaPrivateKey;
        this.dhPublicKey = dhPublicKey;
        this.dhPrivateKey = dhPrivateKey;

    }

    public User(String id, String username, int isVendor, RsaKey rsaPublicKey, DhKey dhPublicKey) {
        this(id, username, isVendor, rsaPublicKey, null, dhPublicKey, null);
    }

    public static User create(Database db, String username, String password, boolean isVendor) throws SQLException {
        var connection = db.getConnection();
        var sql = "INSERT INTO user (id, username, password, is_vendor, salt, "
                + "rsa_public_key, rsa_private_key, dh_public_key, dh_private_key, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            var uuid = Utils.newUuid();
            var salt = Random.instance().nextBytes(64);
            var passwordHash = Hash.passwordHash(password, salt, "password hash", 32);
            var desKey = Hash.passwordHash(password, salt, "private key encryption", 16);
            var rsaKeys = Rsa.generateKeys(2048);
            var rsaPublicKey = rsaKeys.pub().toByteArray();
            var rsaPrivateKey = Des.encryptCbc(rsaKeys.priv().toByteArray(), desKey);
            var dhKeys = Dh.generateKeys();
            var dhPublicKey = dhKeys.pub().toByteArray();
            var dhPrivateKey = Des.encryptCbc(dhKeys.priv().toByteArray(), desKey);
            var timestamp = System.currentTimeMillis();
            statement.setString(1, uuid);
            statement.setString(2, username);
            statement.setBytes(3, passwordHash);
            statement.setInt(4, isVendor ? 1 : 0);
            statement.setBytes(5, salt);
            statement.setBytes(6, rsaPublicKey);
            statement.setBytes(7, rsaPrivateKey);
            statement.setBytes(8, dhPublicKey);
            statement.setBytes(9, dhPrivateKey);
            statement.setLong(10, timestamp);
            statement.execute();
            connection.commit();
            return new User(uuid, username, isVendor ? 1 : 0, rsaKeys.pub(), rsaKeys.priv(), dhKeys.pub(),
                    dhKeys.priv());
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    public static void changePassword(Database db, User user, String password) throws SQLException {
        var connection = db.getConnection();
        var sql = "UPDATE user SET salt = ?, password = ?, rsa_private_key = ?, dh_private_key = ? WHERE id = ?";
        try (var statement = connection.prepareStatement(sql)) {
            var salt = Random.instance().nextBytes(64);
            var passwordHash = Hash.passwordHash(password, salt, "password hash", 32);
            var desKey = Hash.passwordHash(password, salt, "private key encryption", 16);
            var rsaPrivateKey = Des.encryptCbc(user.rsaPrivateKey.toByteArray(), desKey);
            var dhPrivateKey = Des.encryptCbc(user.dhPrivateKey.toByteArray(), desKey);
            statement.setBytes(1, salt);
            statement.setBytes(2, passwordHash);
            statement.setBytes(3, rsaPrivateKey);
            statement.setBytes(4, dhPrivateKey);
            statement.setString(5, user.id);
            statement.execute();
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    public static User getUser(Database db, String username, String password) throws Exception {
        var connection = db.getConnection();
        var sql = "SELECT id, username, password, salt, is_vendor, rsa_private_key, rsa_public_key, "
                + "dh_private_key, dh_public_key FROM user WHERE UPPER(username) = UPPER(?)";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            var results = statement.executeQuery();
            if (results.next()) {
                var salt = results.getBytes("salt");
                var passwordHash = Hash.passwordHash(password, salt, "password hash", 32);
                if (!Arrays.equals(passwordHash, results.getBytes("password"))) {
                    return null;
                }
                var rsaPublicKey = RsaKey.fromByteArray(results.getBytes("rsa_public_key"));
                var dhPublicKey = DhKey.fromByteArray(results.getBytes("dh_public_key"));
                var desKey = Hash.passwordHash(password, salt, "private key encryption", 16);
                var rsaPrivKeyBytes = Des.decryptCbc(results.getBytes("rsa_private_key"), desKey);
                var rsaPrivateKey = RsaKey.fromByteArray(rsaPrivKeyBytes);
                var dhPrivKeyBytes = Des.decryptCbc(results.getBytes("dh_private_key"), desKey);
                var dhPrivateKey = DhKey.fromByteArray(dhPrivKeyBytes);
                return new User(results.getString("id"), results.getString("username"),
                        results.getInt("is_vendor"), rsaPublicKey, rsaPrivateKey, dhPublicKey, dhPrivateKey);
            } else {
                return null;
            }
        } finally {
            connection.rollback();
        }
    }

    public static User getUser(Database db, String id) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT id, username, is_vendor, rsa_public_key, dh_public_key FROM user WHERE id = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            var results = statement.executeQuery();
            if (results.next()) {
                var rsaPublicKey = RsaKey.fromByteArray(results.getBytes("rsa_public_key"));
                var dhPublicKey = DhKey.fromByteArray(results.getBytes("dh_public_key"));
                return new User(results.getString("id"), results.getString("username"),
                        results.getInt("is_vendor"), rsaPublicKey, dhPublicKey);
            } else {
                return null;
            }
        } finally {
            connection.rollback();
        }
    }

    public static boolean isUsernameFree(Database db, String username) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT id FROM user WHERE UPPER(username) = UPPER(?)";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            var results = statement.executeQuery();
            if (results.next() && results.getString("id") != null) {
                return false;
            } else {
                return true;
            }
        } finally {
            connection.rollback();
        }
    }
}
