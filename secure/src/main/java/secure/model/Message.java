package secure.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import secure.*;

public class Message {
    public record MessageSenderReceiver(Message message, User sender, User receiver) {
    }

    public final String id;
    public final String senderId;
    public final String receiverId;
    public final String message;
    public final LocalDateTime createdAt;

    public Message(String id, String senderId, String receiverId, String message, LocalDateTime createAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.createdAt = createAt;
    }

    public static Message create(Database db, String senderId, String receiverId, String message) throws SQLException {
        var connection = db.getConnection();
        var sql = "INSERT INTO message (id, sender_id, receiver_id, message) VALUES (?, ?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            var uuid = Utils.newUuid();
            statement.setString(1, uuid);
            statement.setString(2, senderId);
            statement.setString(3, receiverId);
            statement.setString(4, message);
            statement.execute();
            connection.commit();
            return new Message(uuid, senderId, receiverId, message, LocalDateTime.now());
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public static List<MessageSenderReceiver> getLastInvolving(Database db, String userId) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT (CASE WHEN sender_id = ? THEN receiver_id ELSE sender_id END) AS other_id, "
                + "message.id, sender_id, receiver_id, message, MAX(message.created_at) AS created_at, "
                + "sender.username as sender_username, sender.password AS sender_password, "
                + "receiver.username AS receiver_username, receiver.password AS receiver_password, "
                + "sender.is_vendor AS sender_is_vendor, receiver.is_vendor AS receiver_is_vendor "
                + "FROM message JOIN user AS sender ON (message.sender_id = sender.id) "
                + "JOIN user AS receiver ON (message.receiver_id = receiver.id) "
                + "WHERE (sender_id = ? OR receiver_id = ?) "
                + " GROUP BY other_id"
                + " ORDER BY message.created_at DESC";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, userId);
            statement.setString(3, userId);
            var result = statement.executeQuery();
            var reviews = new ArrayList<MessageSenderReceiver>();
            while (result.next()) {
                reviews.add(
                        new MessageSenderReceiver(
                                new Message(result.getString("id"), result.getString("sender_id"),
                                        result.getString("receiver_id"), result.getString("message"),
                                        result.getTimestamp("created_at").toLocalDateTime()),
                                new User(result.getString("sender_id"), result.getString("sender_username"),
                                        result.getString("sender_password"), result.getInt("sender_is_vendor")),
                                new User(result.getString("receiver_id"), result.getString("receiver_username"),
                                        result.getString("receiver_password"), result.getInt("receiver_is_vendor"))));
            }
            return reviews;
        }
    }

    public static List<MessageSenderReceiver> getBetween(Database db, String userId, String otherId) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT message.id, sender_id, receiver_id, message, message.created_at, "
                + "sender.username as sender_username, sender.password AS sender_password, "
                + "receiver.username AS receiver_username, receiver.password AS receiver_password, "
                + "sender.is_vendor AS sender_is_vendor, receiver.is_vendor AS receiver_is_vendor "
                + "FROM message JOIN user AS sender ON (message.sender_id = sender.id) "
                + "JOIN user AS receiver ON (message.receiver_id = receiver.id) "
                + "WHERE (sender_id = ? AND receiver_id = ?) "
                + "OR (sender_id = ? AND receiver_id = ?) "
                + " ORDER BY message.created_at";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, otherId);
            statement.setString(3, otherId);
            statement.setString(4, userId);
            var result = statement.executeQuery();
            var reviews = new ArrayList<MessageSenderReceiver>();
            while (result.next()) {
                reviews.add(
                        new MessageSenderReceiver(
                                new Message(result.getString("id"), result.getString("sender_id"),
                                        result.getString("receiver_id"), result.getString("message"),
                                        result.getTimestamp("created_at").toLocalDateTime()),
                                new User(result.getString("sender_id"), result.getString("sender_username"),
                                        result.getString("sender_password"), result.getInt("sender_is_vendor")),
                                new User(result.getString("receiver_id"), result.getString("receiver_username"),
                                        result.getString("receiver_password"), result.getInt("receiver_is_vendor"))));
            }
            return reviews;
        }
    }
}
