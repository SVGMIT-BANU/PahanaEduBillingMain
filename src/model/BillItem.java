package model;

public class BillItem {
    private int itemId;
    private String name;
    private double price;
    private int quantity;
    private double total;

    public BillItem() {}

    public BillItem(int itemId, String name, double price, int quantity) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        updateTotal();
    }

    // Getters and setters
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) {
        this.price = price;
        updateTotal();
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        updateTotal();
    }

    //public double getTotal() { return total; }
    public double getTotal() {
        updateTotal();
        return total;
    }

    private void updateTotal() {
        this.total = this.price * this.quantity;
    }

    // New method to force recalculation if needed
    public void recalculateTotal() {
        updateTotal();
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
