package btvn.it210_project.controller;

import btvn.it210_project.model.entity.User;
import btvn.it210_project.service.MovieService;
import btvn.it210_project.service.ShowtimeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CustomerController {

    private final MovieService movieService;
    private final ShowtimeService showtimeService;
    private final btvn.it210_project.repository.UserRepository userRepository;

    // Hàm kiểm tra xem khách đã đăng nhập chưa
    private boolean isCustomer(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "CUSTOMER".equals(user.getRole());
    }

    // 1. Hiển thị danh sách Phim đang chiếu
    @GetMapping("/movies")
    public String showMoviesPage(HttpSession session, Model model) {
        // Có thể cho phép cả Admin xem giao diện này nếu muốn test,
        // nhưng chuẩn nhất là chặn lại nếu chưa đăng nhập
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";

        // Lấy danh sách toàn bộ phim và đẩy xuống giao diện
        model.addAttribute("movies", movieService.getAllMovies());
        return "customer/movie-list";
    }

    private final btvn.it210_project.service.BookingService bookingService;

    @GetMapping("/history")
    public String showHistoryPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        // Lấy danh sách lịch sử đặt vé (CORE-07)
        model.addAttribute("bookings", bookingService.getCustomerHistory(user.getId()));
        return "customer/history";
    }

    // 1. Lấy thông tin Profile hiển thị lên form
    @GetMapping("/profile")
    public String showProfilePage(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) return "redirect:/login";

        // Phải gọi DB để lấy thông tin mới nhất (tránh việc session bị lưu data cũ)
        User currentUser = userRepository.findById(sessionUser.getId()).orElseThrow();
        model.addAttribute("user", currentUser);
        return "customer/profile";
    }

    // 2. Xử lý cập nhật thông tin
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam String phone,
                                @RequestParam String email,
                                HttpSession session,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {

        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) return "redirect:/login";

        User currentUser = userRepository.findById(sessionUser.getId()).orElseThrow();

        // Nếu User chưa có Profile (Tài khoản mới tạo), thì khởi tạo Profile mới
        if (currentUser.getUserProfile() == null) {
            btvn.it210_project.model.entity.UserProfile newProfile = new btvn.it210_project.model.entity.UserProfile();
            newProfile.setUser(currentUser);
            currentUser.setUserProfile(newProfile);
        }

        // Cập nhật dữ liệu
        currentUser.getUserProfile().setFullName(fullName);
        currentUser.getUserProfile().setPhone(phone);
        currentUser.getUserProfile().setEmail(email);

        userRepository.save(currentUser);

        // Cập nhật lại session để góc phải màn hình đổi tên theo
        session.setAttribute("loggedInUser", currentUser);

        ra.addFlashAttribute("success", "Cập nhật hồ sơ thành công!");
        return "redirect:/profile";
    }
}