package btvn.it210_project.controller;

import btvn.it210_project.model.entity.User;
import btvn.it210_project.repository.TicketRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final TicketRepository ticketRepository;

    @GetMapping
    public String showRevenueReport(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login"; // Cấm cửa nếu không phải Admin
        }

        // Gọi DB lấy dữ liệu thô (Mảng Object gồm [Tên phim, Tổng tiền])
        List<Object[]> rawData = ticketRepository.getRevenueByMovie();

        // Tách ra làm 2 danh sách riêng biệt để ném vào biểu đồ (Trục X và Trục Y)
        List<String> movieTitles = new ArrayList<>();
        List<Double> revenues = new ArrayList<>();

        for (Object[] row : rawData) {
            movieTitles.add((String) row[0]);
            revenues.add((Double) row[1]);
        }

        // Đẩy xuống giao diện
        model.addAttribute("movieTitles", movieTitles);
        model.addAttribute("revenues", revenues);

        return "admin/report"; // Chuyển sang file giao diện
    }
}