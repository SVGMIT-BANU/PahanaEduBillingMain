package api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.ItemDao;
import model.Item;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ItemApi implements HttpHandler {

    private final ItemDao dao = new ItemDao();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:5174");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            switch (method.toUpperCase()) {
                case "GET":
                    if (path.equals("/items/search")) handleGetSearch(exchange);
                    else if (path.equals("/items")) handleGetAll(exchange);
                    else if (path.matches("/items/\\d+")) handleGetById(exchange);
                    else sendResponse(exchange, "Not Found", 404);
                    break;
                case "POST": handleAdd(exchange); break;
                case "PUT": handleUpdate(exchange); break;
                case "DELETE": handleDelete(exchange); break;
                default: sendResponse(exchange, "Method Not Allowed", 405);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, "Server Error: " + e.getMessage(), 500);
        }
    }

    private void handleGetById(HttpExchange exchange) throws IOException, SQLException {
        String[] segments = exchange.getRequestURI().getPath().split("/");
        if (segments.length != 3) { sendResponse(exchange, "Invalid path", 400); return; }

        try {
            int id = Integer.parseInt(segments[2]);
            Item item = dao.getItemById(id);
            if (item != null) sendJsonResponse(exchange, gson.toJson(item));
            else sendResponse(exchange, "Item not found", 404);
        } catch (NumberFormatException e) {
            sendResponse(exchange, "Invalid ID format", 400);
        }
    }

    private void handleGetSearch(HttpExchange exchange) throws IOException, SQLException {
        URI uri = exchange.getRequestURI();
        String query = uri.getQuery();
        final String searchTerm = (query != null && query.startsWith("q=")) ? query.substring(2).toLowerCase() : "";

        List<Item> allItems = dao.getAllItems();
        List<Item> filtered = allItems.stream()
                .filter(i -> i.getName().toLowerCase().contains(searchTerm) ||
                        i.getCategory().toLowerCase().contains(searchTerm) ||
                        (i.getIsbn() != null && i.getIsbn().toLowerCase().contains(searchTerm)) ||
                        (i.getAuthor() != null && i.getAuthor().toLowerCase().contains(searchTerm)))
                .collect(Collectors.toList());

        sendJsonResponse(exchange, gson.toJson(filtered));
    }

    private void handleGetAll(HttpExchange exchange) throws IOException, SQLException {
        sendJsonResponse(exchange, gson.toJson(dao.getAllItems()));
    }

    private void handleAdd(HttpExchange exchange) throws IOException, SQLException {
        Item item = readRequestBody(exchange);

        // Validation
        String error = item.validate(false);
        if (error != null) { sendResponse(exchange, error, 400); return; }

        // Duplicate ISBN check
        if (item.getIsbn() != null && dao.isIsbnExists(item.getIsbn())) {
            sendResponse(exchange, "Item ISBN already exists", 409);
            return;
        }

        boolean success = dao.addItem(item);
        sendResponse(exchange, success ? "Added" : "Failed", success ? 200 : 400);
    }

    private void handleUpdate(HttpExchange exchange) throws IOException, SQLException {
        Item item = readRequestBody(exchange);
        if (item.getId() == 0) { sendResponse(exchange, "Item ID missing", 400); return; }

        // Validation
        String error = item.validate(true);
        if (error != null) { sendResponse(exchange, error, 400); return; }

        // Duplicate ISBN check
        if (item.getIsbn() != null && dao.isIsbnExistsForOther(item.getIsbn(), item.getId())) {
            sendResponse(exchange, "Item ISBN already exists for another item", 409);
            return;
        }

        boolean success = dao.updateItem(item);
        sendResponse(exchange, success ? "Updated" : "Failed", success ? 200 : 400);
    }

    private void handleDelete(HttpExchange exchange) throws IOException, SQLException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("id=")) { sendResponse(exchange, "Missing or invalid ID", 400); return; }

        try {
            int id = Integer.parseInt(query.split("=")[1]);
            boolean success = dao.deleteItem(id);
            sendResponse(exchange, success ? "Deleted" : "Failed", success ? 200 : 400);
        } catch (NumberFormatException e) {
            sendResponse(exchange, "Invalid ID format", 400);
        }
    }

    private Item readRequestBody(HttpExchange exchange) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
        return gson.fromJson(reader, Item.class);
    }

    private void sendJsonResponse(HttpExchange exchange, String json) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(response); }
    }

    private void sendResponse(HttpExchange exchange, String message, int code) throws IOException {
        byte[] response = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(code, response.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(response); }
    }
}


