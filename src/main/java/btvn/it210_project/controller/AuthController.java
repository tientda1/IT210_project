package btvn.it210_project.controller;

import btvn.it210_project.model.entity.User;
import btvn.it210_project.model.entity.UserProfile;
import btvn.it210_project.model.dto.LoginDTO;
import btvn.it210_project.model.dto.RegisterDTO;
import btvn.it210_project.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginDTO", new LoginDTO());
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute("loginDTO") LoginDTO loginDTO,
                               BindingResult bindingResult,
                               HttpSession session, Model model) {

        if (bindingResult.hasErrors()) {
            return "login";
        }

        User user = authService.login(loginDTO.getUsername(), loginDTO.getPassword());

        if (user != null) {
            session.setAttribute("loggedInUser", user);
            return "redirect:/dashboard";
        }

        model.addAttribute("error", "Sai tài khoản hoặc mật khẩu!");
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("registerDTO") RegisterDTO registerDTO,
                                  BindingResult bindingResult, Model model) {

        // 1. Kiểm tra lỗi validation từ form
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // 2. Tạo đối tượng User
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setRole("CUSTOMER");

        // 3. Tạo đối tượng UserProfile
        UserProfile profile = new UserProfile();
        profile.setFullName(registerDTO.getFullName());
        profile.setEmail(registerDTO.getEmail());
        profile.setPhone(registerDTO.getPhone());

        // 4. Thiết lập mối liên kết 2 chiều
        profile.setUser(user);
        user.setUserProfile(profile);

        // 5. Lưu vào Database thông qua Service
        if (authService.register(user, registerDTO.getRawPassword())) {
            model.addAttribute("successMsg", "Đăng ký thành công! Vui lòng đăng nhập.");

            model.addAttribute("loginDTO", new LoginDTO());

            return "login";
        }

        model.addAttribute("error", "Tên đăng nhập, Email hoặc Số điện thoại đã được sử dụng!");
        return "register";
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}