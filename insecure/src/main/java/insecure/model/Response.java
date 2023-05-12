package insecure.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import insecure.Database;

public class Response {
    public record ResponseUser(Response response, User user) {
    }

    public record ReviewUserResponses(Review review, User user, List<ResponseUser> responses) {
    }

    public final int id;
    public final int reviewId;
    public final int userId;
    public final String comment;
    public final Timestamp createdAt;

    public Response(int id, int reviewId, int userId, String comment, Timestamp createdAt) {
        this.id = id;
        this.reviewId = reviewId;
        this.userId = userId;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public static Response create(Database db, int reviewId, int userId, String comment) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            statement.execute("INSERT INTO response (review_id, user_id, comment) VALUES (" + reviewId + ", " + userId
                    + ", '" + comment + "')");
            var keys = statement.getGeneratedKeys();
            int id = -1;
            if (keys.next()) {
                id = keys.getInt(1);
            } else {
                throw new RuntimeException("No key returned from INSERT INTO review");
            }
            connection.commit();
            return new Response(id, reviewId, userId, comment, Timestamp.valueOf(LocalDateTime.now()));
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public static List<ReviewUserResponses> getForProduct(Database db, int productId) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            var result = statement.executeQuery(
                    "SELECT review.id AS review_id, review.user_id AS review_user_id, product_id, rating, review.comment AS review_comment, "
                            + "review.created_at AS review_created_at, review.user_id AS review_user_id, user.username AS user_username, user.password AS user_password, "
                            + "response.id AS response_id, response.user_id AS response_user_id, response.comment AS response_comment, "
                            + "response.created_at AS response_created_at, u2.username AS u2_username, u2.password AS u2_password "
                            + "FROM review JOIN user ON (review.user_id = user.id) "
                            + "LEFT JOIN response ON (review.id = response.review_id) LEFT JOIN user AS u2 ON (response.user_id = u2.id) "
                            + "WHERE product_id = " + productId
                            + " ORDER BY review.created_at, review.id, response.created_at");
            var reviews = new ArrayList<ReviewUserResponses>();
            while (result.next()) {
                if (reviews.isEmpty() || reviews.get(reviews.size() - 1).review.id != result.getInt("review_id")) {
                    reviews.add(new ReviewUserResponses(
                            new Review(result.getInt("review_id"), result.getInt("review_user_id"), result.getInt("product_id"),
                                    result.getInt("rating"), result.getString("review_comment"),
                                    result.getTimestamp("review_created_at")),
                            new User(result.getInt("review_user_id"), result.getString("user_username"),
                                    result.getString("user_password")),
                            new ArrayList<>()));
                }
                if (result.getString("response_comment") != null) {
                    reviews.get(reviews.size() - 1).responses.add(
                            new ResponseUser(
                                    new Response(result.getInt("response_id"), result.getInt("product_id"),
                                            result.getInt("response_user_id"), result.getString("response_comment"),
                                            result.getTimestamp("response_created_at")),
                                    new User(result.getInt("response_user_id"), result.getString("u2_username"),
                                            result.getString("u2_password"))));
                }
            }
            return reviews;
        }
    }
}
