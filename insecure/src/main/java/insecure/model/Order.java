package insecure.model;

import java.sql.*;
import java.time.LocalDateTime;

import insecure.Database;
import java.io.Serializable;

import java.util.*;

public class Order implements Serializable {
    public record OrderProductUser(Order order, Product product, User user) {
    }

    public final int id;
    public final int productId;
    public final int userId;
    public final LocalDateTime createdAt;

    public Order(int id, int productId, int userId, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public static Order create(Database db, int productId, int userId) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            statement.execute("INSERT INTO `order` (product_id, user_id) VALUES (" + productId + ", " + userId + ")");
            var keys = statement.getGeneratedKeys();
            int id = -1;
            if (keys.next()) {
                id = keys.getInt(1);
            } else {
                throw new RuntimeException("No key returned from INSERT INTO order");
            }
            connection.commit();
            return new Order(id, productId, userId, LocalDateTime.now());
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public static List<OrderProductUser> getByUser(Database db, int userId) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            var result = statement.executeQuery(
                    "SELECT `order`.id, product_id, `order`.user_id, `order`.created_at, name, price, username, password, is_vendor, "
                            + "product.user_id as product_user_id "
                            + "FROM `order` JOIN product ON (product_id = product.id) "
                            + "JOIN user ON (product.user_id = user.id) WHERE `order`.user_id = "
                            + userId + " ORDER BY `order`.created_at DESC");
            var orders = new ArrayList<OrderProductUser>();
            while (result.next()) {
                orders.add(
                        new OrderProductUser(
                                new Order(result.getInt("id"), result.getInt("product_id"), result.getInt("user_id"),
                                        result.getTimestamp("created_at").toLocalDateTime()),
                                new Product(result.getInt("product_id"), result.getString("name"),
                                        result.getInt("price"), result.getInt("user_id")),
                                new User(result.getInt("product_user_id"), result.getString("username"),
                                        result.getString("password"),
                                        result.getInt("is_vendor"))));
            }
            return orders;
        }
    }

    public static List<OrderProductUser> getForUser(Database db, int userId) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            var result = statement.executeQuery(
                    "SELECT `order`.id, product_id, `order`.user_id, `order`.created_at, name, price, username, password, is_vendor "
                            + "FROM `order` JOIN product ON (product_id = product.id) "
                            + "JOIN user ON (`order`.user_id = user.id) WHERE `product`.user_id = "
                            + userId + " ORDER BY `order`.created_at DESC");
            var orders = new ArrayList<OrderProductUser>();
            while (result.next()) {
                orders.add(
                        new OrderProductUser(
                                new Order(result.getInt("id"), result.getInt("product_id"), result.getInt("user_id"),
                                        result.getTimestamp("created_at").toLocalDateTime()),
                                new Product(result.getInt("product_id"), result.getString("name"),
                                        result.getInt("price"), result.getInt("user_id")),
                                new User(result.getInt("user_id"), result.getString("username"),
                                        result.getString("password"),
                                        result.getInt("is_vendor"))));
            }
            return orders;
        }
    }
}
