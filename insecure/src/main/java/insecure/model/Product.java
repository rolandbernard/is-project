package insecure.model;

import java.io.Serializable;
import java.sql.SQLException;

import insecure.Database;
import java.util.ArrayList;
import java.util.List;

public class Product implements Serializable {
    public record ProductVendor(Product product, User vendor) {
    }

    public final int id;
    public final String name;
    public final float price;
    public final int userId;

    public Product(int id, String name, int price, int userId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.userId = userId;
    }

    public static Product create(Database db, String name, int price, int userId) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            statement.execute("INSERT INTO product (name, price, user_id) VALUES ('" + name + "', " + price + ", "
                    + userId + ")");
            var keys = statement.getGeneratedKeys();
            int id = -1;
            if (keys.next()) {
                id = keys.getInt(1);
            } else {
                throw new RuntimeException("No key returned from INSERT INTO product");
            }
            connection.commit();
            return new Product(id, name, price, userId);
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    public static ProductVendor getProduct(Database db, int id) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            var results = statement.executeQuery(
                    "SELECT product.id as product_id, name, price, user_id, username, password, is_vendor "
                            + "FROM product JOIN user ON (user_id = user.id) WHERE product.id = " + id);
            if (results.next()) {
                return new ProductVendor(new Product(results.getInt("product_id"), results.getString("name"),
                        results.getInt("price"), results.getInt("user_id")),
                        new User(results.getInt("user_id"), results.getString("username"),
                                results.getString("password"), results.getInt("is_vendor")));
            } else {
                return null;
            }
        } finally {
            connection.rollback();
        }
    }

    public static List<Product> getProducts(Database db, int userId) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            var results = statement.executeQuery(
                    "SELECT id, name, price, user_id FROM product WHERE user_id = " + userId
                            + " ORDER BY created_at DESC");
            var products = new ArrayList<Product>();
            while (results.next()) {
                products.add(new Product(results.getInt("id"), results.getString("name"), results.getInt("price"),
                        results.getInt("user_id")));
            }
            return products;
        } finally {
            connection.rollback();
        }
    }

    public static List<Product> search(Database db, String keyword) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            var results = statement.executeQuery(
                    "SELECT id, name, price, user_id FROM product WHERE name LIKE '%" + keyword
                            + "%' ORDER BY created_at DESC");
            var products = new ArrayList<Product>();
            while (results.next()) {
                products.add(new Product(results.getInt("id"), results.getString("name"), results.getInt("price"),
                        results.getInt("user_id")));
            }
            return products;
        } finally {
            connection.rollback();
        }
    }

    public static List<Product> getProducts(Database db) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            var results = statement.executeQuery(
                    "SELECT id, name, price, user_id FROM product ORDER BY created_at DESC");
            var products = new ArrayList<Product>();
            while (results.next()) {
                products.add(new Product(results.getInt("id"), results.getString("name"), results.getInt("price"),
                        results.getInt("user_id")));
            }
            return products;
        } finally {
            connection.rollback();
        }
    }
}
