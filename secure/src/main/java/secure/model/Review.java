package secure.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import secure.*;
import secure.Rsa.*;
import secure.Dh.*;

public class Review {
    public record ReviewUser(Review review, User user) {
    }

    public final String id;
    public final String userId;
    public final String productId;
    public final int rating;
    public final String comment;
    public final LocalDateTime createdAt;

    public Review(String id, String userId, String productId, int rating, String comment, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = Utils.toLocalDateTime(createdAt);
    }

    public static Review create(Database db, String userId, String productId, int rating, String comment)
            throws SQLException {
        var connection = db.getConnection();
        var sql = "INSERT INTO review (id, user_id, product_id, rating, comment, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            var uuid = Utils.newUuid();
            var timestamp = System.currentTimeMillis();
            statement.setString(1, uuid);
            statement.setString(2, userId);
            statement.setString(3, productId);
            statement.setInt(4, rating);
            statement.setString(5, comment);
            statement.setLong(6, timestamp);
            statement.execute();
            connection.commit();
            return new Review(uuid, userId, productId, rating, comment, timestamp);
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    public static Review getReview(Database db, String reviewId) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT review.id, user_id, product_id, rating, comment, review.created_at "
                + "FROM review WHERE id = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, reviewId);
            var result = statement.executeQuery();
            if (result.next()) {
                return new Review(result.getString("id"), result.getString("user_id"), result.getString("product_id"),
                        result.getInt("rating"), result.getString("comment"), result.getLong("created_at"));
            } else {
                return null;
            }
        }
    }

    public static List<ReviewUser> getForProduct(Database db, String productId) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT review.id, user_id, product_id, rating, comment, review.created_at, username, password, is_vendor, rsa_public_key, dh_public_key "
                + "FROM review JOIN user ON (user_id = user.id) "
                + "WHERE product_id = ? ORDER BY review.created_at DESC";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, productId);
            var result = statement.executeQuery();
            var reviews = new ArrayList<ReviewUser>();
            while (result.next()) {
                var rsaPublicKey = RsaKey.fromByteArray(result.getBytes("rsa_public_key"));
                var dhPublicKey = DhKey.fromByteArray(result.getBytes("dh_public_key"));
                reviews.add(
                        new ReviewUser(
                                new Review(result.getString("id"), result.getString("user_id"),
                                        result.getString("product_id"), result.getInt("rating"),
                                        result.getString("comment"), result.getLong("created_at")),
                                new User(result.getString("user_id"), result.getString("username"),
                                        result.getInt("is_vendor"), rsaPublicKey, dhPublicKey)));
            }
            return reviews;
        }
    }
}
