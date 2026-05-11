package btvn.it210_project.controller;

import btvn.it210_project.model.entity.Showtime;
import btvn.it210_project.repository.MovieRepository;
import btvn.it210_project.repository.RoomRepository;
import btvn.it210_project.service.ShowtimeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/admin/showtimes")
@RequiredArgsConstructor
public class AdminShowtimeController {

    private final ShowtimeService showtimeService;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    private boolean isAdmin(HttpSession session) {
        btvn.it210_project.model.entity.User user = (btvn.it210_project.model.entity.User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    // 1. Hiển thị danh sách Lịch chiếu
    @GetMapping
    public String listShowtimes(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        // Lấy tất cả lịch chiếu (bạn có thể viết thêm hàm getAllShowtimes trong service nếu chưa có)
        model.addAttribute("showtimes", showtimeService.getAllShowtimes());
        return "admin/showtime-list";
    }

    // 2. Form thêm Lịch chiếu
    @GetMapping("/add")
    public String showAddForm(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("showtime", new Showtime());
        // Truyền danh sách Phim và Phòng xuống để làm Dropdown (thẻ <select>)
        model.addAttribute("movies", movieRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
        return "admin/showtime-form";
    }

    // 3. Xử lý lưu Lịch chiếu và Check Xung đột
    @PostMapping("/save")
    public String saveShowtime(@Valid @ModelAttribute("showtime") Showtime showtime,
                               BindingResult bindingResult,
                               HttpSession session, Model model,
                               RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/login";

        // Nếu validation thất bại (ví dụ: ngày quá khứ, giá âm)
        if (bindingResult.hasErrors()) {
            // Phải đẩy lại danh sách Phim và Phòng để thẻ <select> không bị trắng tinh
            model.addAttribute("movies", movieRepository.findAll());
            model.addAttribute("rooms", roomRepository.findAll());
            return "admin/showtime-form";
        }

        boolean isSuccess = showtimeService.createShowtime(showtime);
        if (!isSuccess) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: Phòng này đã có lịch chiếu hoặc đang dọn dẹp!");
            return "redirect:/admin/showtimes/add";
        }

        redirectAttributes.addFlashAttribute("success", "Thêm suất chiếu thành công!");
        return "redirect:/admin/showtimes";
    }
}