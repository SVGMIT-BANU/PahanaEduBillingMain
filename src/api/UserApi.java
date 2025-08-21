package api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.UserDAO;
import factory.UserFactory;
import model.User;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UserApi implements HttpHandler {

    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals("/users/register") && method.equalsIgnoreCase("POST")) {
            handleRegister(exchange);
        } else if (path.equals("/users/login") && method.equalsIgnoreCase("POST")) {
            handleLogin(exchange);
        } else if (path.equals("/users/delete") && method.equalsIgnoreCase("DELETE")) {
            handleDelete(exchange);
        } else {
            sendResponse(exchange, 404, "Not Found");
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines().reduce("", (acc, line) -> acc + line);
        Map<String, String> data = gson.fromJson(body, Map.class);
        String username = data.get("username");
        String password = data.get("password");
        String role = data.get("role");

        if (username == null || password == null || role == null) {
            sendResponse(exchange, 400, "Missing fields");
            return;
        }

        if (!"staff".equalsIgnoreCase(role)) {
            sendResponse(exchange, 403, "Only staff registration allowed");
            return;
        }

        User user = UserFactory.createUser("staff", username, password);
        boolean success = userDAO.registerUser(user);
        sendResponse(exchange, success ? 200 : 500, success ? "Staff registered" : "Registration failed");
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines().reduce("", (acc, line) -> acc + line);
        Map<String, String> data = gson.fromJson(body, Map.class);
        String username = data.get("username");
        String password = data.get("password");

        User user = userDAO.loginUser(username, password);
        sendResponse(exchange, user != null ? 200 : 401, user != null ? gson.toJson(user) : "Invalid credentials");
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines().reduce("", (acc, line) -> acc + line);
        Map<String, String> data = gson.fromJson(body, Map.class);
        String username = data.get("username");

        if (username == null) {
            sendResponse(exchange, 400, "Username required");
            return;
        }

        boolean success = userDAO.deleteUser(username);
        sendResponse(exchange, success ? 200 : 404, success ? "User deleted" : "User not found");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}



