package model;

import java.util.List;

public class Bill {
    private int id;
    private int customerId;
    private String customerName;
    private List<BillItem> items;
    private double subtotal;
    private double tax;
    private double total;
    private String status;     // New field
    private String billDate;   // New field

    public Bill() {}

    public Bill(int customerId, String customerName, List<BillItem> items) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.items = items;
        this.status = "Pending"; // Default status
        calculateTotals();
    }

    public void calculateTotals() {
        subtotal = 0;
        if (items != null) {
            for (BillItem item : items) {
                subtotal += item.getTotal();
            }
        }
        tax = subtotal * 0.10;
        total = subtotal + tax;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public List<BillItem> getItems() { return items; }
    public void setItems(List<BillItem> items) {
        this.items = items;
        calculateTotals();  // Recalculate totals when items are set
    }

    public double getSubtotal() {
        calculateTotals();
        return subtotal;
    }

    public double getTax() {
        calculateTotals();
        return tax;
    }

    public double getTotal() {
        calculateTotals();
        return total;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBillDate() { return billDate; }
    public void setBillDate(String billDate) { this.billDate = billDate; }
}
