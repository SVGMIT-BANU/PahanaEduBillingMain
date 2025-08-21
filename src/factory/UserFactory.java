package factory;

import model.User;
import org.mindrot.jbcrypt.BCrypt;

public class UserFactory {

    public static User createUser(String role, String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setRole(role);

        // Password hash செய்யும் போது
        if ("admin".equalsIgnoreCase(role)) {
            // Fixed password for admin
            String fixedPassword = "admin123";
            String hashed = BCrypt.hashpw(fixedPassword, BCrypt.gensalt(10));
            user.setPassword(hashed);
        } else {
            // Staff password hash
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt(10));
            user.setPassword(hashed);
        }

        return user;
    }
}
