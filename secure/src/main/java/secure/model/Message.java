package secure.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import secure.Database;

public class Message {
    public record MessageSenderReceiver(Message message, User sender, User receiver) {
    }

    public final int id;
    public final int senderId;
    public final int receiverId;
    public final String message;
    public final LocalDateTime createdAt;

    public Message(int id, int senderId, int receiverId, String message, LocalDateTime createAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.createdAt = createAt;
    }

    public static Message create(Database db, int senderId, int receiverId, String message) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            statement.execute(
                    "INSERT INTO message (sender_id, receiver_id, message) VALUES (" + senderId + ", " + receiverId
                            + ", '" + message + "')");
            var keys = statement.getGeneratedKeys();
            int id = -1;
            if (keys.next()) {
                id = keys.getInt(1);
            } else {
                throw new RuntimeException("No key returned from INSERT INTO message");
            }
            connection.commit();
            return new Message(id, senderId, receiverId, message, LocalDateTime.now());
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public static List<MessageSenderReceiver> getLastInvolving(Database db, int userId) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            var result = statement.executeQuery(
                    "SELECT (CASE WHEN sender_id = " + userId + " THEN receiver_id ELSE sender_id END) AS other_id, "
                            + "message.id, sender_id, receiver_id, message, MAX(message.created_at) AS created_at, "
                            + "sender.username as sender_username, sender.password AS sender_password, "
                            + "receiver.username AS receiver_username, receiver.password AS receiver_password, "
                            + "sender.is_vendor AS sender_is_vendor, receiver.is_vendor AS receiver_is_vendor "
                            + "FROM message JOIN user AS sender ON (message.sender_id = sender.id) "
                            + "JOIN user AS receiver ON (message.receiver_id = receiver.id) "
                            + "WHERE (sender_id = " + userId + " OR receiver_id = " + userId + ") "
                            + " GROUP BY other_id"
                            + " ORDER BY message.created_at DESC");
            var reviews = new ArrayList<MessageSenderReceiver>();
            while (result.next()) {
                reviews.add(
                        new MessageSenderReceiver(
                                new Message(result.getInt("id"), result.getInt("sender_id"),
                                        result.getInt("receiver_id"), result.getString("message"),
                                        result.getTimestamp("created_at").toLocalDateTime()),
                                new User(result.getInt("sender_id"), result.getString("sender_username"),
                                        result.getString("sender_password"), result.getInt("sender_is_vendor")),
                                new User(result.getInt("receiver_id"), result.getString("receiver_username"),
                                        result.getString("receiver_password"), result.getInt("receiver_is_vendor"))));
            }
            return reviews;
        }
    }

    public static List<MessageSenderReceiver> getBetween(Database db, int userId, int otherId) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            var result = statement.executeQuery(
                    "SELECT message.id, sender_id, receiver_id, message, message.created_at, "
                            + "sender.username as sender_username, sender.password AS sender_password, "
                            + "receiver.username AS receiver_username, receiver.password AS receiver_password, "
                            + "sender.is_vendor AS sender_is_vendor, receiver.is_vendor AS receiver_is_vendor "
                            + "FROM message JOIN user AS sender ON (message.sender_id = sender.id) "
                            + "JOIN user AS receiver ON (message.receiver_id = receiver.id) "
                            + "WHERE (sender_id = " + userId + " AND receiver_id = " + otherId + ") "
                            + "OR (sender_id = " + otherId + " AND receiver_id = " + userId + ") "
                            + " ORDER BY message.created_at");
            var reviews = new ArrayList<MessageSenderReceiver>();
            while (result.next()) {
                reviews.add(
                        new MessageSenderReceiver(
                                new Message(result.getInt("id"), result.getInt("sender_id"),
                                        result.getInt("receiver_id"), result.getString("message"),
                                        result.getTimestamp("created_at").toLocalDateTime()),
                                new User(result.getInt("sender_id"), result.getString("sender_username"),
                                        result.getString("sender_password"), result.getInt("sender_is_vendor")),
                                new User(result.getInt("receiver_id"), result.getString("receiver_username"),
                                        result.getString("receiver_password"), result.getInt("receiver_is_vendor"))));
            }
            return reviews;
        }
    }
}
