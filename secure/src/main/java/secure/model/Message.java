package secure.model;

import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import secure.*;
import secure.Rsa.RsaKey;
import secure.Dh.*;

public class Message {
    public record MessageSenderReceiver(Message message, User sender, User receiver) {
    }

    public final String id;
    public final String senderId;
    public final String receiverId;
    public final String message;
    public final LocalDateTime createdAt;

    public Message(String id, String senderId, String receiverId, String message, long createAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.createdAt = Utils.toLocalDateTime(createAt);
    }

    public static Message create(Database db, User sender, String receiverId, String message) throws SQLException {
        var connection = db.getConnection();
        var sql = "INSERT INTO message (id, sender_id, receiver_id, message, created_at) VALUES (?, ?, ?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            var uuid = Utils.newUuid();
            var timestamp = System.currentTimeMillis();
            var receiver = User.getUser(db, receiverId);
            var desKey = generateKey(sender, receiver);
            var encryptedMessage = Des.encryptCbc(message.getBytes("UTF-8"), desKey);
            statement.setString(1, uuid);
            statement.setString(2, sender.id);
            statement.setString(3, receiverId);
            statement.setBytes(4, encryptedMessage);
            statement.setLong(5, timestamp);
            statement.execute();
            connection.commit();
            return new Message(uuid, sender.id, receiverId, message, timestamp);
        } catch (UnsupportedEncodingException e) {
            throw Utils.panic(e);
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    public static List<MessageSenderReceiver> getLastInvolving(Database db, User currentUser) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT (CASE WHEN sender_id = ? THEN receiver_id ELSE sender_id END) AS other_id, "
                + "message.id, sender_id, receiver_id, message, MAX(message.created_at) AS created_at, "
                + "sender.username as sender_username, sender.rsa_public_key AS sender_rsa_public_key, "
                + "receiver.username AS receiver_username, receiver.rsa_public_key AS receiver_rsa_public_key, "
                + "receiver.dh_public_key AS receiver_dh_public_key, sender.dh_public_key AS sender_dh_public_key, "
                + "sender.is_vendor AS sender_is_vendor, receiver.is_vendor AS receiver_is_vendor "
                + "FROM message JOIN user AS sender ON (message.sender_id = sender.id) "
                + "JOIN user AS receiver ON (message.receiver_id = receiver.id) "
                + "WHERE (sender_id = ? OR receiver_id = ?) "
                + " GROUP BY other_id"
                + " ORDER BY message.created_at DESC";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, currentUser.id);
            statement.setString(2, currentUser.id);
            statement.setString(3, currentUser.id);
            var result = statement.executeQuery();
            var reviews = new ArrayList<MessageSenderReceiver>();
            while (result.next()) {
                var sender = new User(result.getString("sender_id"), result.getString("sender_username"),
                        result.getInt("sender_is_vendor"),
                        RsaKey.fromByteArray(result.getBytes("sender_rsa_public_key")),
                        DhKey.fromByteArray(result.getBytes("sender_dh_public_key")));
                var receiver = new User(result.getString("receiver_id"), result.getString("receiver_username"),
                        result.getInt("receiver_is_vendor"),
                        RsaKey.fromByteArray(result.getBytes("receiver_rsa_public_key")),
                        DhKey.fromByteArray(result.getBytes("receiver_dh_public_key")));
                var other = sender.id.equals(currentUser.id) ? receiver : sender;
                var message = decryptMessage(result.getBytes("message"), generateKey(currentUser, other));
                reviews.add(
                        new MessageSenderReceiver(
                                new Message(result.getString("id"), result.getString("sender_id"),
                                        result.getString("receiver_id"), message,
                                        result.getLong("created_at")),
                                sender, receiver));
            }
            return reviews;
        }
    }

    public static List<MessageSenderReceiver> getBetween(Database db, User currentUser, String otherId)
            throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT message.id, sender_id, receiver_id, message, message.created_at, "
                + "sender.username as sender_username, sender.rsa_public_key AS sender_rsa_public_key, "
                + "receiver.username AS receiver_username, receiver.rsa_public_key AS receiver_rsa_public_key, "
                + "receiver.dh_public_key AS receiver_dh_public_key, sender.dh_public_key AS sender_dh_public_key, "
                + "sender.is_vendor AS sender_is_vendor, receiver.is_vendor AS receiver_is_vendor "
                + "FROM message JOIN user AS sender ON (message.sender_id = sender.id) "
                + "JOIN user AS receiver ON (message.receiver_id = receiver.id) "
                + "WHERE (sender_id = ? AND receiver_id = ?) "
                + "OR (sender_id = ? AND receiver_id = ?) "
                + " ORDER BY message.created_at";
        try (var statement = connection.prepareStatement(sql)) {
            var other = User.getUser(db, otherId);
            var desKey = generateKey(currentUser, other);
            statement.setString(1, currentUser.id);
            statement.setString(2, otherId);
            statement.setString(3, otherId);
            statement.setString(4, currentUser.id);
            var result = statement.executeQuery();
            var reviews = new ArrayList<MessageSenderReceiver>();
            while (result.next()) {
                var message = decryptMessage(result.getBytes("message"), desKey);
                reviews.add(
                        new MessageSenderReceiver(
                                new Message(result.getString("id"), result.getString("sender_id"),
                                        result.getString("receiver_id"), message,
                                        result.getLong("created_at")),
                                new User(result.getString("sender_id"), result.getString("sender_username"),
                                        result.getInt("sender_is_vendor"),
                                        RsaKey.fromByteArray(result.getBytes("sender_rsa_public_key")),
                                        DhKey.fromByteArray(result.getBytes("sender_dh_public_key"))),
                                new User(result.getString("receiver_id"), result.getString("receiver_username"),
                                        result.getInt("receiver_is_vendor"),
                                        RsaKey.fromByteArray(result.getBytes("receiver_rsa_public_key")),
                                        DhKey.fromByteArray(result.getBytes("receiver_dh_public_key")))));
            }
            return reviews;
        }
    }

    private static byte[] generateKey(User current, User other) {
        return Dh.generateKey(new DhKeys(current.dhPrivateKey, current.dhPublicKey), other.dhPublicKey, 16);
    }

    private static String decryptMessage(byte[] encryptedMessage, byte[] desKey) {
        var decryptedMessage = Des.decryptCbc(encryptedMessage, desKey);
        try {
            return new String(decryptedMessage, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw Utils.panic(e);
        }
    }
}
