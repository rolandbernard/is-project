package secure.model;

import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import secure.*;
import secure.Rsa.*;
import secure.Dh.*;

public class Product implements Serializable {
    public record ProductVendor(Product product, User vendor) {
    }

    public final String id;
    public final String name;
    public final long price;
    public final String description;
    public final byte[] image;
    public final String userId;
    public final LocalDateTime createdAt;

    public Product(String id, String name, long price, String description, byte[] image, String userId,
            long createdAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.image = image;
        this.userId = userId;
        this.createdAt = Utils.toLocalDateTime(createdAt);
    }

    public static Product create(Database db, String name, long price, String description, byte[] image, String userId)
            throws SQLException {
        var connection = db.getConnection();
        var sql = "INSERT INTO product (id, name, price, description, image, user_id, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            var uuid = Utils.newUuid();
            var timeStamp = System.currentTimeMillis();
            statement.setString(1, uuid);
            statement.setString(2, name);
            statement.setLong(3, price);
            statement.setString(4, description);
            statement.setBytes(5, image);
            statement.setString(6, userId);
            statement.setLong(7, timeStamp);
            statement.execute();
            connection.commit();
            return new Product(uuid, name, price, description, image, userId, timeStamp);
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    public static ProductVendor getProduct(Database db, String id) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT product.id as product_id, name, price, description, image, user_id, product.created_at, "
                + "username, rsa_public_key, dh_public_key, is_vendor "
                + "FROM product JOIN user ON (user_id = user.id) WHERE product.id = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            var results = statement.executeQuery();
            if (results.next()) {
                return new ProductVendor(
                        new Product(results.getString("product_id"), results.getString("name"),
                                results.getLong("price"), results.getString("description"), results.getBytes("image"),
                                results.getString("user_id"), results.getLong("created_at")),
                        new User(results.getString("user_id"), results.getString("username"),
                                results.getInt("is_vendor"), RsaKey.fromByteArray(results.getBytes("rsa_public_key")),
                                DhKey.fromByteArray(results.getBytes("dh_public_key"))));
            } else {
                return null;
            }
        } finally {
            connection.rollback();
        }
    }

    public static List<Product> getProducts(Database db, String userId) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT id, name, price, description, image, user_id, created_at FROM product WHERE user_id = ? ORDER BY created_at DESC";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            var results = statement.executeQuery();
            var products = new ArrayList<Product>();
            while (results.next()) {
                products.add(new Product(results.getString("id"), results.getString("name"), results.getLong("price"),
                        results.getString("description"), results.getBytes("image"), results.getString("user_id"),
                        results.getLong("created_at")));
            }
            return products;
        } finally {
            connection.rollback();
        }
    }

    public static List<Product> search(Database db, String keyword) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT id, name, price, description, image, user_id, created_at FROM product WHERE name LIKE ? ORDER BY created_at DESC";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + keyword + "%");
            var results = statement.executeQuery();
            var products = new ArrayList<Product>();
            while (results.next()) {
                products.add(new Product(results.getString("id"), results.getString("name"), results.getLong("price"),
                        results.getString("description"), results.getBytes("image"), results.getString("user_id"),
                        results.getLong("created_at")));
            }
            return products;
        } finally {
            connection.rollback();
        }
    }

    public static List<Product> getProducts(Database db) throws SQLException {
        var connection = db.getConnection();
        var sql = "SELECT id, name, price, description, image, user_id, created_at FROM product ORDER BY created_at DESC";
        try (var statement = connection.prepareStatement(sql)) {
            var results = statement.executeQuery();
            var products = new ArrayList<Product>();
            while (results.next()) {
                products.add(new Product(results.getString("id"), results.getString("name"), results.getLong("price"),
                        results.getString("description"), results.getBytes("image"), results.getString("user_id"),
                        results.getLong("created_at")));
            }
            return products;
        } finally {
            connection.rollback();
        }
    }
}
