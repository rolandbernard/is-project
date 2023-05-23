package secure.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.io.Serializable;

import java.util.*;

import secure.*;
import secure.Rsa.*;

public class Order implements Serializable {
    public record OrderProductUser(Order order, Product product, User user) {
    }

    public final String id;
    public final String productId;
    public final String userId;
    public final LocalDateTime createdAt;
    public final boolean isValid;

    public Order(String id, String productId, String userId, long createdAt, boolean isValid) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.createdAt = Utils.toLocalDateTime(createdAt);
        this.isValid = isValid;
    }

    public static Order create(Database db, String productId, User user) throws SQLException {
        var connection = db.getConnection();
        var sql = "INSERT INTO `order` (id, product_id, user_id, signature, created_at) VALUES (?, ?, ?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            var timeStamp = System.currentTimeMillis();
            var uuid = Utils.newUuid();
            statement.setString(1, uuid);
            statement.setString(2, productId);
            statement.setString(3, user.id);
            statement.setBytes(4, createSignature(user, uuid, productId, timeStamp));
            statement.setLong(5, timeStamp);
            statement.execute();
            connection.commit();
            return new Order(uuid, productId, user.id, timeStamp, true);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public static List<OrderProductUser> getByUser(Database db, String userId) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT `order`.id, product_id, `order`.user_id, `order`.created_at, name, price, product.user_id as product_user_id, signature, "
                + "vendor.username AS vendor_username, vendor.public_key AS vendor_public_key, vendor.is_vendor AS vendor_is_vendor, "
                + "buyer.id AS buyer_id, buyer.username AS buyer_username, buyer.public_key AS buyer_public_key, buyer.is_vendor AS buyer_is_vendor "
                + "FROM `order` JOIN product ON (product_id = product.id) "
                + "JOIN user AS buyer ON (`order`.user_id = buyer.id) "
                + "JOIN user AS vendor ON (product.user_id = vendor.id) WHERE `order`.user_id = ? "
                + "ORDER BY `order`.created_at DESC";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            var result = statement.executeQuery();
            var orders = new ArrayList<OrderProductUser>();
            while (result.next()) {
                var buyer = new User(result.getString("buyer_id"), result.getString("buyer_username"),
                        result.getInt("buyer_is_vendor"), RsaKey.fromByteArray(result.getBytes("buyer_public_key")));
                var orderId = result.getString("id");
                var productId = result.getString("product_id");
                var signature = result.getBytes("signature");
                var createdAt = result.getLong("created_at");
                var isValid = validateSignature(orderId, buyer, productId, createdAt, signature);
                orders.add(
                        new OrderProductUser(
                                new Order(orderId, productId, buyer.id, createdAt, isValid),
                                new Product(result.getString("product_id"), result.getString("name"),
                                        result.getInt("price"), result.getString("user_id")),
                                new User(result.getString("product_user_id"), result.getString("vendor_username"),
                                        result.getInt("vendor_is_vendor"), RsaKey.fromByteArray(result.getBytes("vendor_public_key")))));
            }
            return orders;
        }
    }

    public static List<OrderProductUser> getForUser(Database db, String userId) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT `order`.id, product_id, `order`.user_id AS user_id, `order`.created_at AS order_created_at, name, price, username, public_key, signature, is_vendor "
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
                var createdAt = result.getLong("order_created_at");
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

    public static boolean userBoughtProduct(Database db, String userId, String productId) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT id FROM `order` WHERE user_id = ? AND product_id = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, productId);
            var result = statement.executeQuery();
            return result.next() && result.getString("id") != null;
        }
    }

    private static byte[] createSignature(User user, String orderId, String productId, long createdAt) {
        var signatureString = createSignatureString(orderId, user, productId, createdAt);
        return Rsa.sign(signatureString, user.privateKey);
    }

    private static boolean validateSignature(String orderId, User buyer, String productId, long createdAt,
            byte[] signature) {
        var signatureString = createSignatureString(orderId, buyer, productId, createdAt);
        return Rsa.verify(signatureString, signature, buyer.publicKey);
    }

    private static String createSignatureString(String orderId, User user, String productId, long createdAt) {
        return orderId + "." + user.id + "." + productId + "." + createdAt;
    }
}
