package insecure.model;

import java.io.Serializable;
import java.sql.SQLException;

import insecure.Database;
import java.util.ArrayList;
import java.util.List;

public class Product implements Serializable {
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

    public static Product getProduct(Database db, int id) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            var results = statement.executeQuery(
                    "SELECT id, name, price, user_id FROM product WHERE id = " + id);
            if (results.next()) {
                return new Product(results.getInt("id"), results.getString("name"), results.getInt("price"),
                        results.getInt("user_id"));
            } else {
                return null;
            }
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    public static List<Product> getProducts(Database db, int userId) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            var results = statement.executeQuery(
                    "SELECT id, name, price, user_id FROM product WHERE user_id = " + userId);
            var products = new ArrayList<Product>();
            while (results.next()) {
                products.add(new Product(results.getInt("id"), results.getString("name"), results.getInt("price"),
                        results.getInt("user_id")));
            }
            return products;
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    public static List<Product> getProducts(Database db) throws SQLException {
        var connection = db.getConnection();
        try (var statement = connection.createStatement()) {
            var results = statement.executeQuery(
                    "SELECT id, name, price, user_id FROM product");
            var products = new ArrayList<Product>();
            while (results.next()) {
                products.add(new Product(results.getInt("id"), results.getString("name"), results.getInt("price"),
                        results.getInt("user_id")));
            }
            return products;
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }
}
