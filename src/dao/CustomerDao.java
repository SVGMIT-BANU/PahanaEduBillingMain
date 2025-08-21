package dao;

import db.DBConnection;
import model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        String query = "SELECT * FROM customers";
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            list.add(new Customer(
                    rs.getInt("id"),
                    rs.getString("account_no"),
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getString("telephone"),
                    rs.getString("email"),
                    rs.getDouble("total_purchases")
            ));
        }
        return list;
    }

    public Customer getCustomerById(int id) throws SQLException {
        Customer customer = null;
        Connection conn = DBConnection.getConnection();
        String query = "SELECT * FROM customers WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            customer = new Customer(
                    rs.getInt("id"),
                    rs.getString("account_no"),
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getString("telephone"),
                    rs.getString("email"),
                    rs.getDouble("total_purchases")
            );

        }

        return customer;
    }


    public boolean addCustomer(Customer customer) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String query = "INSERT INTO customers (account_no, name, address, telephone, email, total_purchases) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, customer.getAccountNo());
        stmt.setString(2, customer.getName());
        stmt.setString(3, customer.getAddress());
        stmt.setString(4, customer.getTelephone());
        stmt.setString(5, customer.getEmail());
        stmt.setDouble(6, customer.getTotalPurchases());
        return stmt.executeUpdate() > 0;
    }

    public boolean updateCustomer(Customer customer) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String query = "UPDATE customers SET account_no=?, name=?, address=?, telephone=?, email=?, total_purchases=? WHERE id=?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, customer.getAccountNo());
        stmt.setString(2, customer.getName());
        stmt.setString(3, customer.getAddress());
        stmt.setString(4, customer.getTelephone());
        stmt.setString(5, customer.getEmail());
        stmt.setDouble(6, customer.getTotalPurchases());
        stmt.setInt(7, customer.getId());
        return stmt.executeUpdate() > 0;
    }

    public boolean deleteCustomer(int id) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String query = "DELETE FROM customers WHERE id=?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, id);
        return stmt.executeUpdate() > 0;
    }

    public boolean isAccountNoExists(String accountNo) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT COUNT(*) FROM customers WHERE account_no = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, accountNo);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }
}
