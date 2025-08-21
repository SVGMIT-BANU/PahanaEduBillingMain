package service;

import dao.UserDAO;
import factory.UserFactory;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {

    private UserDAO userDAO = new UserDAO();

    // Create admin if not exists
    public void createAdminIfNotExists() {
        if (!userDAO.adminExists()) {
            String adminUsername = "admin";
            String adminPlainPassword = "admin123";

            // BCrypt hash generate
            String hashedPassword = BCrypt.hashpw(adminPlainPassword, BCrypt.gensalt(10));

            // Create User object using factory
            User admin = UserFactory.createUser("admin", adminUsername, hashedPassword);

            // Save to DB
            boolean success = userDAO.registerUser(admin);
            if (success) {
                System.out.println("✅ Admin account created");
                System.out.println("Username: " + adminUsername + " | Password: " + adminPlainPassword);
            } else {
                System.out.println("❌ Failed to create admin account");
            }
        } else {
            System.out.println("ℹ Admin account already exists");
        }
    }


}
