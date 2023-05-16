package secure.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import secure.*;

public class Response {
    public record ResponseUser(Response response, User user) {
    }

    public record ReviewUserResponses(Review review, User user, List<ResponseUser> responses) {
    }

    public final String id;
    public final String reviewId;
    public final String userId;
    public final String comment;
    public final LocalDateTime createdAt;

    public Response(String id, String reviewId, String userId, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.reviewId = reviewId;
        this.userId = userId;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public static Response create(Database db, String reviewId, String userId, String comment) throws SQLException {
        var connection = db.getConnection();
        var sql = "INSERT INTO response (id, review_id, user_id, comment) VALUES (?, ?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            var uuid = Utils.newUuid();
            statement.setString(1, uuid);
            statement.setString(2, reviewId);
            statement.setString(3, userId);
            statement.setString(4, comment);
            statement.execute();
            connection.commit();
            return new Response(uuid, reviewId, userId, comment, LocalDateTime.now());
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public static List<ReviewUserResponses> getForProduct(Database db, String productId) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT review.id AS review_id, review.user_id AS review_user_id, product_id, rating, review.comment AS review_comment, "
                + "review.created_at AS review_created_at, review.user_id AS review_user_id, user.username AS user_username, user.password AS user_password, "
                + "response.id AS response_id, response.user_id AS response_user_id, response.comment AS response_comment, "
                + "response.created_at AS response_created_at, u2.username AS u2_username, u2.password AS u2_password, "
                + "user.is_vendor AS user_is_vendor, u2.is_vendor AS u2_is_vendor "
                + "FROM review JOIN user ON (review.user_id = user.id) "
                + "LEFT JOIN response ON (review.id = response.review_id) LEFT JOIN user AS u2 ON (response.user_id = u2.id) "
                + "WHERE product_id = ? "
                + "ORDER BY review.created_at DESC, review.id, response.created_at";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, productId);
            var result = statement.executeQuery();
            var reviews = new ArrayList<ReviewUserResponses>();
            while (result.next()) {
                if (reviews.isEmpty() || reviews.get(reviews.size() - 1).review.id.equals(result.getString("review_id"))) {
                    reviews.add(new ReviewUserResponses(
                            new Review(result.getString("review_id"), result.getString("review_user_id"),
                                    result.getString("product_id"),
                                    result.getInt("rating"), result.getString("review_comment"),
                                    result.getTimestamp("review_created_at").toLocalDateTime()),
                            new User(result.getString("review_user_id"), result.getString("user_username"),
                                    result.getString("user_password"), result.getInt("user_is_vendor")),
                            new ArrayList<>()));
                }
                if (result.getString("response_comment") != null) {
                    reviews.get(reviews.size() - 1).responses.add(
                            new ResponseUser(
                                    new Response(result.getString("response_id"), result.getString("product_id"),
                                            result.getString("response_user_id"), result.getString("response_comment"),
                                            result.getTimestamp("response_created_at").toLocalDateTime()),
                                    new User(result.getString("response_user_id"), result.getString("u2_username"),
                                            result.getString("u2_password"), result.getInt("u2_is_vendor"))));
                }
            }
            return reviews;
        }
    }
}
