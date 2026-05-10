package btvn.it210_project.utility;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    // Hàm băm mật khẩu
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    // Hàm kiểm tra mật khẩu
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}
