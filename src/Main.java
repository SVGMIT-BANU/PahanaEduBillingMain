import api.BillApi;
import api.CustomerApi;
import api.ItemApi;
import api.UserApi;
import com.sun.net.httpserver.HttpServer;
import dao.UserDAO;
import service.UserService;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            // APIs
            server.createContext("/customers", new CustomerApi());
            server.createContext("/customers/search", new CustomerApi());

            server.createContext("/items", new ItemApi());
            server.createContext("/items/search", new ItemApi());

            server.createContext("/api/bills", new BillApi());

            // User endpoints
            server.createContext("/users/register", new UserApi());
            server.createContext("/users/login", new UserApi());

            new UserDAO().createOrResetAdmin();

            server.setExecutor(null);
            server.start();

            System.out.println("Server started on http://localhost:8080");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
