package secure.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.io.Serializable;

import java.util.*;

import secure.*;
import secure.Rsa.*;
import secure.Dh.*;

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
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    public static OrderProductUser getOrder(Database db, User buyer, String orderId) throws SQLException {
        var sql = "SELECT `order`.id, product_id, product.user_id AS user_id, `order`.created_at AS order_created_at, "
                + " name, price, description, image, product.created_at as product_created_at, "
                + " username, rsa_public_key, dh_public_key, signature, is_vendor "
                + "FROM `order` JOIN product ON (product_id = product.id) "
                + "JOIN user ON (product.user_id = user.id) WHERE `order`.user_id = ? AND `order`.id = ?"
                + "ORDER BY `order`.created_at DESC";
        var connection = db.getConnection();
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, buyer.id);
            statement.setString(2, orderId);
            var result = statement.executeQuery();
            if (result.next()) {
                var signature = result.getBytes("signature");
                var createdAt = result.getLong("order_created_at");
                var product = new Product(result.getString("product_id"), result.getString("name"),
                        result.getLong("price"), result.getString("description"), result.getBytes("image"),
                        result.getString("user_id"), result.getLong("product_created_at"));
                var seller = new User(result.getString("user_id"), result.getString("username"),
                        result.getInt("is_vendor"), RsaKey.fromByteArray(result.getBytes("rsa_public_key")),
                        DhKey.fromByteArray(result.getBytes("dh_public_key")));
                var isValid = validateSignature(orderId, buyer, product.id, createdAt, signature);
                return new OrderProductUser(new Order(orderId, product.id, buyer.id, createdAt, isValid), product,
                        seller);
            } else {
                return null;
            }
        }
    }

    public static List<OrderProductUser> getByUser(Database db, User buyer) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT `order`.id, product_id, product.user_id AS user_id, `order`.created_at AS order_created_at, "
                + " name, price, description, image, product.created_at as product_created_at, "
                + " username, rsa_public_key, dh_public_key, signature, is_vendor "
                + "FROM `order` JOIN product ON (product_id = product.id) "
                + "JOIN user ON (product.user_id = user.id) WHERE `order`.user_id = ? "
                + "ORDER BY `order`.created_at DESC";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, buyer.id);
            var result = statement.executeQuery();
            var orders = new ArrayList<OrderProductUser>();
            while (result.next()) {
                var orderId = result.getString("id");
                var signature = result.getBytes("signature");
                var createdAt = result.getLong("order_created_at");
                var product = new Product(result.getString("product_id"), result.getString("name"),
                        result.getLong("price"), result.getString("description"), result.getBytes("image"),
                        result.getString("user_id"), result.getLong("product_created_at"));
                var seller = new User(result.getString("user_id"), result.getString("username"),
                        result.getInt("is_vendor"), RsaKey.fromByteArray(result.getBytes("rsa_public_key")),
                        DhKey.fromByteArray(result.getBytes("dh_public_key")));
                var isValid = validateSignature(orderId, buyer, product.id, createdAt, signature);
                orders.add(new OrderProductUser(new Order(orderId, product.id, buyer.id, createdAt, isValid), product,
                        seller));
            }
            return orders;
        }
    }

    public static List<OrderProductUser> getForUser(Database db, User seller) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT `order`.id, product_id, `order`.user_id AS user_id, `order`.created_at AS order_created_at, "
                + " name, price, description, image, product.created_at as product_created_at, "
                + " username, rsa_public_key, dh_public_key, signature, is_vendor "
                + "FROM `order` JOIN product ON (product_id = product.id) "
                + "JOIN user ON (`order`.user_id = user.id) WHERE product.user_id = ? "
                + "ORDER BY `order`.created_at DESC";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, seller.id);
            var result = statement.executeQuery();
            var orders = new ArrayList<OrderProductUser>();
            while (result.next()) {
                var orderId = result.getString("id");
                var signature = result.getBytes("signature");
                var createdAt = result.getLong("order_created_at");
                var product = new Product(result.getString("product_id"), result.getString("name"),
                        result.getLong("price"), result.getString("description"), result.getBytes("image"),
                        result.getString("user_id"), result.getLong("product_created_at"));
                var buyer = new User(result.getString("user_id"), result.getString("username"),
                        result.getInt("is_vendor"), RsaKey.fromByteArray(result.getBytes("rsa_public_key")),
                        DhKey.fromByteArray(result.getBytes("dh_public_key")));
                var isValid = validateSignature(orderId, buyer, product.id, createdAt, signature);
                orders.add(new OrderProductUser(new Order(orderId, product.id, buyer.id, createdAt, isValid), product,
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
        return Rsa.sign(signatureString, user.rsaPrivateKey);
    }

    private static boolean validateSignature(String orderId, User buyer, String productId, long createdAt,
            byte[] signature) {
        var signatureString = createSignatureString(orderId, buyer, productId, createdAt);
        return Rsa.verify(signatureString, signature, buyer.rsaPublicKey);
    }

    private static String createSignatureString(String orderId, User user, String productId, long createdAt) {
        return orderId + "." + user.id + "." + productId + "." + createdAt;
    }
}
