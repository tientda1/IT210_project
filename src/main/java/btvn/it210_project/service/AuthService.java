package btvn.it210_project.service;

import btvn.it210_project.model.entity.User;
import btvn.it210_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    public boolean register(User user, String rawPassword) {
        // 1. Kiểm tra trùng Username
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            return false;
        }

        user.setPasswordHash(rawPassword);

        // 2. Dùng try-catch để bắt lỗi trùng Email hoặc Số điện thoại từ Database
        try {
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            // Nếu lưu thất bại (do MySQL báo lỗi trùng email/phone), trả về false
            return false;
        }
    }

    public User login(String username, String password) {
        // Tìm user theo username
        Optional<User> optionalUser = userRepository.findByUsername(username);

        // Nếu tìm thấy user
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // So sánh mật khẩu (Khi nào ráp BCrypt thì đổi thành BCrypt.checkpw)
            if (user.getPasswordHash().equals(password)) {
                return user;
            }
        }
        return null;
    }

}