package api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.CustomerDao;
import model.Customer;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerApi implements HttpHandler {
    private final CustomerDao dao = new CustomerDao();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // ✅ CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:5174");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // ✅ Preflight OPTIONS
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                if (path.equals("/customers/search")) {
                    handleGetSearch(exchange);
                } else if (path.equals("/customers")) {
                    handleGetAll(exchange);
                } else if (path.matches("/customers/\\d+")) {
                    handleGetById(exchange);
                } else {
                    sendError(exchange, "Not Found", 404);
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                handleAdd(exchange);
            } else if ("PUT".equalsIgnoreCase(method)) {
                handleUpdate(exchange);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                handleDelete(exchange);
            } else {
                sendError(exchange, "Method Not Allowed", 405);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(exchange, "Server Error", 500);
        }
    }

    // ---------------- GET Handlers ----------------

    private void handleGetAll(HttpExchange exchange) throws IOException, SQLException {
        List<Customer> list = dao.getAllCustomers();
        String json = gson.toJson(list);
        sendJsonResponse(exchange, json);
    }

    private void handleGetById(HttpExchange exchange) throws IOException, SQLException {
        String path = exchange.getRequestURI().getPath(); // /customers/3
        String[] parts = path.split("/");
        if (parts.length != 3) {
            sendError(exchange, "Invalid URL format", 400);
            return;
        }

        try {
            int id = Integer.parseInt(parts[2]);
            Customer customer = dao.getCustomerById(id);
            if (customer != null) {
                String json = gson.toJson(customer);
                sendJsonResponse(exchange, json);
            } else {
                sendError(exchange, "Customer not found", 404);
            }
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid ID format", 400);
        }
    }

    private void handleGetSearch(HttpExchange exchange) throws IOException, SQLException {
        URI uri = exchange.getRequestURI();
        String query = uri.getQuery();

        final String searchTerm;
        if (query != null && query.startsWith("q=")) {
            searchTerm = query.substring(2).toLowerCase();
        } else {
            searchTerm = "";
        }

        List<Customer> allCustomers = dao.getAllCustomers();

        List<Customer> filtered;
        if (searchTerm.isEmpty()) {
            filtered = allCustomers;
        } else {
            filtered = allCustomers.stream()
                    .filter(c ->
                            c.getName().toLowerCase().contains(searchTerm) ||
                                    c.getAccountNo().toLowerCase().contains(searchTerm) ||
                                    c.getTelephone().contains(searchTerm))
                    .collect(Collectors.toList());
        }

        String json = gson.toJson(filtered);
        sendJsonResponse(exchange, json);
    }

    // ---------------- POST / PUT / DELETE ----------------

    private void handleAdd(HttpExchange exchange) throws IOException, SQLException {
        Customer customer = readRequestBody(exchange);

        String validationError = validateCustomer(customer, false);
        if (validationError != null) {
            sendError(exchange, validationError, 400);
            return;
        }

        if (dao.isAccountNoExists(customer.getAccountNo())) {
            sendError(exchange, "Customer account number already exists", 409);
            return;
        }

        boolean success = dao.addCustomer(customer);
        if (success) {
            sendJsonResponse(exchange, gson.toJson(customer)); // return added customer
        } else {
            sendError(exchange, "Failed to add customer", 400);
        }
    }

    private void handleUpdate(HttpExchange exchange) throws IOException, SQLException {
        Customer customer = readRequestBody(exchange);

        String validationError = validateCustomer(customer, true);
        if (validationError != null) {
            sendError(exchange, validationError, 400);
            return;
        }

        boolean success = dao.updateCustomer(customer);
        if (success) {
            sendJsonResponse(exchange, gson.toJson(customer)); // return updated customer
        } else {
            sendError(exchange, "Failed to update customer", 400);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException, SQLException {
        String query = exchange.getRequestURI().getQuery(); // e.g., id=1

        if (query == null || !query.matches("id=\\d+")) {
            sendError(exchange, "Missing or invalid ID", 400);
            return;
        }

        int id = Integer.parseInt(query.substring(3));
        boolean success = dao.deleteCustomer(id);
        if (success) {
            sendJsonResponse(exchange, "{\"message\":\"Deleted\"}");
        } else {
            sendError(exchange, "Failed to delete customer", 400);
        }
    }

    // ---------------- Validation ----------------

    private String validateCustomer(Customer customer, boolean isUpdate) {
        if (customer == null) {
            return "Customer data is missing";
        }
        if (isUpdate && customer.getId() <= 0) {
            return "Invalid or missing customer ID";
        }
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            return "Customer name is required";
        }
        if (customer.getAccountNo() == null || customer.getAccountNo().trim().isEmpty()) {
            return "Account number is required";
        }
        if (!customer.getAccountNo().matches("[A-Za-z0-9_-]{4,20}")) {
            return "Account number must be 4–20 characters (letters, digits, _, -)";
        }
        if (customer.getTelephone() == null || customer.getTelephone().trim().isEmpty()) {
            return "Telephone number is required";
        }
        if (!customer.getTelephone().matches("\\d{10,15}")) {
            return "Telephone must be 10–15 digits";
        }
        if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
            if (!customer.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,6}$")) {
                return "Invalid email format";
            }
        }
        return null; // ✅ no validation errors
    }

    // ---------------- Utility Methods ----------------

    private Customer readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            return gson.fromJson(reader, Customer.class);
        }
    }

    private void sendJsonResponse(HttpExchange exchange, String json) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    private void sendError(HttpExchange exchange, String message, int code) throws IOException {
        String json = gson.toJson(new ErrorResponse(code, message));
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(code, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    // ✅ Error Response helper class
    private static class ErrorResponse {
        private final int status;
        private final String message;

        public ErrorResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}
