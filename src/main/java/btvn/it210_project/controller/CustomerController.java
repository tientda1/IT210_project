package btvn.it210_project.controller;

import btvn.it210_project.model.entity.User;
import btvn.it210_project.service.MovieService;
import btvn.it210_project.service.ShowtimeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class CustomerController {

    private final MovieService movieService;
    private final ShowtimeService showtimeService;

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

    @GetMapping("/profile")
    public String showProfilePage(HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";
        return "customer/profile";
    }
}