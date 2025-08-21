

package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Bill;
import model.BillItem;

public class BillDAO {
    public int saveBill(Bill bill) throws SQLException {
        if (bill.getCustomerName() != null && !bill.getCustomerName().trim().isEmpty()) {
            String insertBillSQL = "INSERT INTO bills (customer_id, customer_name, subtotal, tax, total) VALUES (?, ?, ?, ?, ?)";
            String insertBillItemSQL = "INSERT INTO bill_items (bill_id, item_id, name, price, quantity, total) VALUES (?, ?, ?, ?, ?, ?)";

            int var21;
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);

                try (PreparedStatement billStmt = conn.prepareStatement(insertBillSQL, 1)) {
                    billStmt.setInt(1, bill.getCustomerId());
                    billStmt.setString(2, bill.getCustomerName());
                    billStmt.setDouble(3, bill.getSubtotal());
                    billStmt.setDouble(4, bill.getTax());
                    billStmt.setDouble(5, bill.getTotal());
                    int affectedRows = billStmt.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating bill failed, no rows affected.");
                    }

                    try (ResultSet generatedKeys = billStmt.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            throw new SQLException("Creating bill failed, no ID obtained.");
                        }

                        int billId = generatedKeys.getInt(1);

                        try (PreparedStatement itemStmt = conn.prepareStatement(insertBillItemSQL)) {
                            for(BillItem item : bill.getItems()) {
                                itemStmt.setInt(1, billId);
                                itemStmt.setInt(2, item.getItemId());
                                itemStmt.setString(3, item.getName());
                                itemStmt.setDouble(4, item.getPrice());
                                itemStmt.setInt(5, item.getQuantity());
                                itemStmt.setDouble(6, item.getTotal());
                                itemStmt.addBatch();
                            }

                            itemStmt.executeBatch();
                        }

                        conn.commit();
                        var21 = billId;
                    }
                } catch (SQLException e) {
                    conn.rollback();
                    System.err.println("Transaction rolled back due to: " + e.getMessage());
                    throw e;
                }
            }

            return var21;
        } else {
            throw new IllegalArgumentException("Customer name cannot be null or empty");
        }
    }

    public List<Bill> getAllBills() throws SQLException {
        List<Bill> bills = new ArrayList();
        String sql = "SELECT * FROM bills ORDER BY bill_date DESC";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
        ) {
            while(rs.next()) {
                Bill bill = new Bill();
                bill.setId(rs.getInt("id"));
                bill.setCustomerId(rs.getInt("customer_id"));
                bill.setCustomerName(rs.getString("customer_name"));
                bill.setSubtotal(rs.getDouble("subtotal"));
                bill.setTax(rs.getDouble("tax"));
                bill.setTotal(rs.getDouble("total"));
                bill.setStatus(rs.getString("status"));
                bill.setBillDate(rs.getTimestamp("bill_date").toString());
                bills.add(bill);
            }
        }

        return bills;
    }

    public boolean deleteBillById(int billId) throws SQLException {
        String deleteItemsSQL = "DELETE FROM bill_items WHERE bill_id = ?";
        String deleteBillSQL = "DELETE FROM bills WHERE id = ?";

        boolean var8;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement deleteItemsStmt = conn.prepareStatement(deleteItemsSQL);
                    PreparedStatement deleteBillStmt = conn.prepareStatement(deleteBillSQL);
            ) {
                deleteItemsStmt.setInt(1, billId);
                deleteItemsStmt.executeUpdate();
                deleteBillStmt.setInt(1, billId);
                int affectedRows = deleteBillStmt.executeUpdate();
                conn.commit();
                var8 = affectedRows > 0;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Rollback on delete due to: " + e.getMessage());
                throw e;
            }
        }

        return var8;
    }

    public Bill getBillById(int billId) throws SQLException {
        String billSQL = "SELECT * FROM bills WHERE id = ?";
        String itemsSQL = "SELECT * FROM bill_items WHERE bill_id = ?";
        Bill bill = null;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement billStmt = conn.prepareStatement(billSQL);
                PreparedStatement itemsStmt = conn.prepareStatement(itemsSQL);
        ) {
            billStmt.setInt(1, billId);

            try (ResultSet billRs = billStmt.executeQuery()) {
                if (!billRs.next()) {
                    Object var23 = null;
                    return (Bill)var23;
                }

                bill = new Bill();
                bill.setId(billId);
                bill.setCustomerId(billRs.getInt("customer_id"));
                bill.setCustomerName(billRs.getString("customer_name"));
                bill.setSubtotal(billRs.getDouble("subtotal"));
                bill.setTax(billRs.getDouble("tax"));
                bill.setTotal(billRs.getDouble("total"));
                bill.setStatus(billRs.getString("status"));
                bill.setBillDate(billRs.getTimestamp("bill_date").toString());
            }

            itemsStmt.setInt(1, billId);

            try (ResultSet itemRs = itemsStmt.executeQuery()) {
                List<BillItem> items = new ArrayList();

                while(itemRs.next()) {
                    BillItem item = new BillItem(itemRs.getInt("item_id"), itemRs.getString("name"), itemRs.getDouble("price"), itemRs.getInt("quantity"));
                    item.setTotal(itemRs.getDouble("total"));
                    items.add(item);
                }

                bill.setItems(items);
                return bill;
            }
        }
    }
}
