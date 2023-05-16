package secure.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.io.Serializable;

import java.util.*;

import secure.*;

public class Order implements Serializable {
    public record OrderProductUser(Order order, Product product, User user) {
    }

    public final String id;
    public final String productId;
    public final String userId;
    public final LocalDateTime createdAt;

    public Order(String id, String productId, String userId, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public static Order create(Database db, String productId, String userId) throws SQLException {
        var connection = db.getConnection();
        var sql = "INSERT INTO `order` (id, product_id, user_id) VALUES (?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            var uuid = Utils.newUuid();
            statement.setString(1, uuid);
            statement.setString(2, productId);
            statement.setString(3, userId);
            statement.execute();
            connection.commit();
            return new Order(uuid, productId, userId, LocalDateTime.now());
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public static List<OrderProductUser> getByUser(Database db, String userId) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT `order`.id, product_id, `order`.user_id, `order`.created_at, name, price, username, password, is_vendor, "
                + "product.user_id as product_user_id "
                + "FROM `order` JOIN product ON (product_id = product.id) "
                + "JOIN user ON (product.user_id = user.id) WHERE `order`.user_id = ? "
                + "ORDER BY `order`.created_at DESC";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            var result = statement.executeQuery();
            var orders = new ArrayList<OrderProductUser>();
            while (result.next()) {
                orders.add(
                        new OrderProductUser(
                                new Order(result.getString("id"), result.getString("product_id"),
                                        result.getString("user_id"),
                                        result.getTimestamp("created_at").toLocalDateTime()),
                                new Product(result.getString("product_id"), result.getString("name"),
                                        result.getInt("price"), result.getString("user_id")),
                                new User(result.getString("product_user_id"), result.getString("username"),
                                        result.getString("password"), result.getInt("is_vendor"))));
            }
            return orders;
        }
    }

    public static List<OrderProductUser> getForUser(Database db, String userId) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT `order`.id, product_id, `order`.user_id, `order`.created_at, name, price, username, password, is_vendor "
                + "FROM `order` JOIN product ON (product_id = product.id) "
                + "JOIN user ON (`order`.user_id = user.id) WHERE `product`.user_id = ? "
                + "ORDER BY `order`.created_at DESC";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            var result = statement.executeQuery();
            var orders = new ArrayList<OrderProductUser>();
            while (result.next()) {
                orders.add(
                        new OrderProductUser(
                                new Order(result.getString("id"), result.getString("product_id"), result.getString("user_id"),
                                        result.getTimestamp("created_at").toLocalDateTime()),
                                new Product(result.getString("product_id"), result.getString("name"),
                                        result.getInt("price"), result.getString("user_id")),
                                new User(result.getString("user_id"), result.getString("username"),
                                        result.getString("password"),
                                        result.getInt("is_vendor"))));
            }
            return orders;
        }
    }
}
