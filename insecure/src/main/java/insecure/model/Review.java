package insecure.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import insecure.Database;

public class Review {
    public record ReviewUser(Review review, User user) {
    }

    public final int id;
    public final int userId;
    public final int productId;
    public final int rating;
    public final String comment;
    public final LocalDateTime createdAt;

    public Review(int id, int userId, int productId, int rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public static Review create(Database db, int userId, int productId, int rating, String comment)
            throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            statement.execute("INSERT INTO review (user_id, product_id, rating, comment) VALUES (" + userId + ", "
                    + productId + ", " + rating + ", '" + comment + "')");
            var keys = statement.getGeneratedKeys();
            int id = -1;
            if (keys.next()) {
                id = keys.getInt(1);
            } else {
                throw new RuntimeException("No key returned from INSERT INTO review");
            }
            connection.commit();
            return new Review(id, userId, productId, rating, comment, LocalDateTime.now());
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    public static List<ReviewUser> getForProduct(Database db, int productId) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            var result = statement.executeQuery(
                    "SELECT review.id, user_id, product_id, rating, comment, review.created_at, username, password, is_vendor "
                            + "FROM review JOIN user ON (user_id = user.id) "
                            + "WHERE product_id = " + productId
                            + " ORDER BY review.created_at DESC");
            var reviews = new ArrayList<ReviewUser>();
            while (result.next()) {
                reviews.add(
                        new ReviewUser(
                                new Review(result.getInt("id"), result.getInt("user_id"), result.getInt("product_id"),
                                        result.getInt("rating"), result.getString("comment"),
                                        result.getTimestamp("created_at").toLocalDateTime()),
                                new User(result.getInt("user_id"), result.getString("username"),
                                        result.getString("password"),
                                        result.getInt("is_vendor"))));
            }
            return reviews;
        }
    }
}
