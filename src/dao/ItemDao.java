package dao;

import db.DBConnection;
import model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDao {

    public List<Item> getAllItems() throws SQLException {
        List<Item> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String query = "SELECT * FROM items";
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            list.add(new Item(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getString("description"),
                    rs.getString("isbn"),
                    rs.getString("author")
            ));
        }
        return list;
    }

    public Item getItemById(int id) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String query = "SELECT * FROM items WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new Item(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getString("description"),
                    rs.getString("isbn"),
                    rs.getString("author")
            );
        }
        return null;
    }

    public boolean addItem(Item item) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String query = "INSERT INTO items (name, category, price, stock, description, isbn, author) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, item.getName());
        stmt.setString(2, item.getCategory());
        stmt.setDouble(3, item.getPrice());
        stmt.setInt(4, item.getStock());
        stmt.setString(5, item.getDescription());
        stmt.setString(6, item.getIsbn());
        stmt.setString(7, item.getAuthor());
        return stmt.executeUpdate() > 0;
    }

    public boolean updateItem(Item item) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String query = "UPDATE items SET name=?, category=?, price=?, stock=?, description=?, isbn=?, author=? WHERE id=?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, item.getName());
        stmt.setString(2, item.getCategory());
        stmt.setDouble(3, item.getPrice());
        stmt.setInt(4, item.getStock());
        stmt.setString(5, item.getDescription());
        stmt.setString(6, item.getIsbn());
        stmt.setString(7, item.getAuthor());
        stmt.setInt(8, item.getId());
        return stmt.executeUpdate() > 0;
    }

    public boolean deleteItem(int id) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String query = "DELETE FROM items WHERE id=?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, id);
        return stmt.executeUpdate() > 0;
    }

    public boolean isIsbnExists(String isbn) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT COUNT(*) FROM items WHERE isbn = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, isbn);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    public boolean isIsbnExistsForOther(String isbn, int id) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT COUNT(*) FROM items WHERE isbn = ? AND id != ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, isbn);
        stmt.setInt(2, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }
}


