package btvn.it210_project.controller;

import btvn.it210_project.model.entity.Movie;
import btvn.it210_project.service.MovieService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/movies")
@RequiredArgsConstructor
public class AdminMovieController {

    private final MovieService movieService;

    // Kiểm tra quyền Admin (Dùng chung cho các hàm)
    private boolean isAdmin(HttpSession session) {
        btvn.it210_project.model.entity.User user = (btvn.it210_project.model.entity.User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    // 1. Hiển thị danh sách phim
    @GetMapping
    public String listMovies(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login"; // Đuổi ra nếu không phải Admin

        model.addAttribute("movies", movieService.getAllMovies());
        return "admin/movie-list";
    }

    // 2. Hiển thị form thêm phim mới
    @GetMapping("/add")
    public String showAddForm(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("movie", new Movie()); // Truyền đối tượng rỗng xuống form
        return "admin/movie-form";
    }

    // 3. Hiển thị form sửa phim
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        Movie movie = movieService.getMovieById(id);
        if (movie == null) return "redirect:/admin/movies";

        model.addAttribute("movie", movie);
        return "admin/movie-form"; // Dùng chung form với chức năng Thêm
    }

    // 4. Xử lý lưu phim (Dùng cho cả Thêm và Sửa)
    @PostMapping("/save")
    public String saveMovie(@ModelAttribute("movie") Movie movie, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        movieService.saveMovie(movie);
        return "redirect:/admin/movies";
    }

    // 5. Xóa phim
    @GetMapping("/delete/{id}")
    public String deleteMovie(@PathVariable Integer id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        movieService.deleteMovie(id);
        return "redirect:/admin/movies";
    }
}