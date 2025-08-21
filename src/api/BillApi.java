
package api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dao.BillDAO;
import model.Bill;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BillApi implements HttpHandler {

    private final BillDAO billDAO = new BillDAO();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers for every request
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:5174");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        String method = exchange.getRequestMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(204, -1); // No body for OPTIONS
            exchange.close();
            return;
        }

        try {
            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    exchange.getResponseHeaders().add("Allow", "GET, POST, DELETE, OPTIONS");
                    sendResponse(exchange, 405, gson.toJson(Map.of("error", "Method Not Allowed")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException, SQLException {
        URI requestURI = exchange.getRequestURI();
        String path = requestURI.getPath();
        // Expected paths:
        // /api/bills           -> list all bills
        // /api/bills/{id}      -> get bill by id
        String[] pathParts = path.split("/");

        if (pathParts.length == 3) {
            // GET all bills
            List<Bill> bills = billDAO.getAllBills();
            String json = gson.toJson(bills);
            sendResponse(exchange, 200, json);
        } else if (pathParts.length == 4) {
            // GET bill by id
            try {
                int billId = Integer.parseInt(pathParts[3]);
                Bill bill = billDAO.getBillById(billId);
                if (bill != null) {
                    String json = gson.toJson(bill);
                    sendResponse(exchange, 200, json);
                } else {
                    sendResponse(exchange, 404, gson.toJson(Map.of("error", "Bill not found")));
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, gson.toJson(Map.of("error", "Invalid bill id")));
            }
        } else {
            sendResponse(exchange, 404, gson.toJson(Map.of("error", "Not Found")));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException, SQLException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String requestBody = br.lines().collect(Collectors.joining());

            Type billType = new TypeToken<Bill>() {}.getType();
            Bill bill = gson.fromJson(requestBody, billType);

            // Validation
            if (bill == null || bill.getItems() == null || bill.getItems().isEmpty()) {
                sendResponse(exchange, 400, gson.toJson(Map.of("error", "Bill must contain at least one item.")));
                return;
            }

            if (bill.getCustomerName() == null || bill.getCustomerName().trim().isEmpty()) {
                sendResponse(exchange, 400, gson.toJson(Map.of("error", "Customer name is required.")));
                return;
            }

            // Totals calculate
            bill.calculateTotals();

            // Save and set generated billId
            int billId = billDAO.saveBill(bill);
            bill.setId(billId); // make sure Bill object has ID

            // Send back full bill object
            sendResponse(exchange, 201, gson.toJson(bill));
        }
    }


    private void handleDelete(HttpExchange exchange) throws IOException, SQLException {
        URI requestURI = exchange.getRequestURI();
        String path = requestURI.getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length == 4) {
            try {
                int billId = Integer.parseInt(pathParts[3]);
                boolean deleted = billDAO.deleteBillById(billId);
                if (deleted) {
                    sendResponse(exchange, 200, gson.toJson(Map.of("message", "Bill deleted successfully")));
                } else {
                    sendResponse(exchange, 404, gson.toJson(Map.of("error", "Bill not found")));
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, gson.toJson(Map.of("error", "Invalid bill id")));
            }
        } else {
            sendResponse(exchange, 400, gson.toJson(Map.of("error", "Bill id required for delete")));
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}

