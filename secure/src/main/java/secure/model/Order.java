package secure.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.io.Serializable;

import java.util.*;

import secure.*;
import secure.Random;
import secure.Rsa.*;

public class Order implements Serializable {
    public record OrderProductUser(Order order, Product product, User user) {
    }

    public final String id;
    public final String productId;
    public final String userId;
    public final LocalDateTime createdAt;
    public final boolean isValid;

    public Order(String id, String productId, String userId, LocalDateTime createdAt, boolean isValid) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.isValid = isValid;
    }

    public static Order create(Database db, String productId, User user) throws SQLException {
        var connection = db.getConnection();
        var sql = "INSERT INTO `order` (id, product_id, user_id, salt, signature) VALUES (?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            var uuid = Utils.newUuid();
            var salt = Random.instance().nextBytes(64);
            statement.setString(1, uuid);
            statement.setString(2, productId);
            statement.setString(3, user.id);
            statement.setBytes(4, salt);
            statement.setBytes(5, createSignature(user, uuid, productId, LocalDateTime.now()));
            statement.execute();
            connection.commit();
            return new Order(uuid, productId, user.id, LocalDateTime.now(), true);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public static List<OrderProductUser> getByUser(Database db, String userId) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT `order`.id, product_id, `order`.user_id, `order`.created_at, name, price, product.user_id as product_user_id "
                + "vendor.username AS product_username, vendor.public_key AS product_public_key, vendor.is_vendor AS vendor_is_vendor, "
                + "buyer.id AS buyer_id, buyer.username AS buyer_username, buyer.public_key AS buyer_public_key, buyer.is_vendor AS buyer_is_vendor "
                + "FROM `order` JOIN product ON (product_id = product.id) "
                + "JOIN user AS buyer ON (`order`.user_id = user.id) "
                + "JOIN user AS vendor ON (product.user_id = user.id) WHERE `order`.user_id = ? "
                + "ORDER BY `order`.created_at DESC";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            var result = statement.executeQuery();
            var orders = new ArrayList<OrderProductUser>();
            while (result.next()) {
                var buyer = new User(result.getString("buyer_id"), result.getString("buyer_username"),
                        result.getInt("buyer_is_vendor"),
                        RsaKey.fromByteArray(result.getBytes("buyer_public_key")));
                var orderId = result.getString("id");
                var productId = result.getString("product_id");
                var signature = result.getBytes("signature");
                var createdAt = result.getTimestamp("created_at").toLocalDateTime();
                var isValid = validateSignature(orderId, buyer, productId, createdAt, signature);
                orders.add(
                        new OrderProductUser(
                                new Order(orderId, productId, buyer.id, createdAt, isValid),
                                new Product(result.getString("product_id"), result.getString("name"),
                                        result.getInt("price"), result.getString("user_id")),
                                new User(result.getString("product_user_id"), result.getString("product_username"),
                                        result.getInt("is_vendor"),
                                        RsaKey.fromByteArray(result.getBytes("public_key")))));
            }
            return orders;
        }
    }

    public static List<OrderProductUser> getForUser(Database db, String userId) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT `order`.id, product_id, `order`.user_id, `order`.created_at, name, price, username, public_key, is_vendor, salt "
                + "FROM `order` JOIN product ON (product_id = product.id) "
                + "JOIN user ON (`order`.user_id = user.id) WHERE `product`.user_id = ? "
                + "ORDER BY `order`.created_at DESC";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            var result = statement.executeQuery();
            var orders = new ArrayList<OrderProductUser>();
            while (result.next()) {
                var orderId = result.getString("id");
                var productId = result.getString("product_id");
                var signature = result.getBytes("signature");
                var createdAt = result.getTimestamp("created_at").toLocalDateTime();
                var buyer = new User(result.getString("user_id"), result.getString("username"),
                        result.getInt("is_vendor"), RsaKey.fromByteArray(result.getBytes("public_key")));
                var isValid = validateSignature(orderId, buyer, productId, createdAt, signature);
                orders.add(
                        new OrderProductUser(
                                new Order(orderId, productId, buyer.id, createdAt, isValid),
                                new Product(result.getString("product_id"), result.getString("name"),
                                        result.getInt("price"), result.getString("user_id")),
                                buyer));
            }
            return orders;
        }
    }

    private static byte[] createSignature(User user, String orderId, String productId, LocalDateTime createdAt) {
        var signatureString = createSignatureString(orderId, user, productId, createdAt);
        return Rsa.sign(signatureString, user.privateKey);
    }

    private static boolean validateSignature(String orderId, User buyer, String productId, LocalDateTime createdAt,
            byte[] signature) {
        var signatureString = createSignatureString(orderId, buyer, productId, createdAt);
        return Rsa.verify(signatureString, signature, buyer.publicKey);
    }

    private static String createSignatureString(String orderId, User user, String productId, LocalDateTime createdAt) {
        return orderId + "." + user.id + "." + productId + "." + createdAt.toString();
    }
}
