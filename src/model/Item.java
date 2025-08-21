package model;

public class Item {
    private int id;
    private String name;
    private String category;
    private double price;
    private int stock;
    private String description;
    private String isbn;
    private String author;

    // Default constructor
    public Item() {}

    // Parameterized constructor
    public Item(int id, String name, String category, double price, int stock,
                String description, String isbn, String author) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.isbn = isbn;
        this.author = author;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getDescription() { return description; }
    public String getIsbn() { return isbn; }
    public String getAuthor() { return author; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void setDescription(String description) { this.description = description; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setAuthor(String author) { this.author = author; }

    public int getQuantity() { return stock; }

    @Override
    public String toString() {
        return "Item{id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", description='" + description + '\'' +
                ", isbn='" + isbn + '\'' +
                ", author='" + author + '\'' +
                '}';
    }

    // Validation method
    public String validate(boolean isUpdate) {
        if (!isUpdate && (name == null || name.trim().isEmpty())) {
            return "Name is required";
        }
        if (category == null || category.trim().isEmpty()) {
            return "Category is required";
        }
        if (price < 0) {
            return "Price cannot be negative";
        }
        if (stock < 0) {
            return "Stock cannot be negative";
        }
        if (isbn != null && !isbn.trim().isEmpty() && isbn.length() < 3) {
            return "ISBN must be at least 3 characters";
        }
        if (author != null && !author.trim().isEmpty() && author.length() < 3) {
            return "Author name must be at least 3 characters";
        }
        return null; // No error
    }
}
