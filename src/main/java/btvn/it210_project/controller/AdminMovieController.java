package btvn.it210_project.controller;

import btvn.it210_project.model.entity.Genre;
import btvn.it210_project.model.entity.Movie;
import btvn.it210_project.service.MovieService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/movies")
@RequiredArgsConstructor
public class AdminMovieController {

    private final MovieService movieService;
    private final btvn.it210_project.repository.GenreRepository genreRepository;

    // Kiểm tra quyền Admin
    private boolean isAdmin(HttpSession session) {
        btvn.it210_project.model.entity.User user = (btvn.it210_project.model.entity.User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    // 1. Hiển thị danh sách phim
    @GetMapping
    public String listMovies(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("movies", movieService.getAllMovies());
        return "admin/movie-list";
    }

    // 2. Hiển thị form thêm phim mới
    @GetMapping("/add")
    public String showAddForm(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("movie", new Movie());
        model.addAttribute("genres", genreRepository.findAll());
        return "admin/movie-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        Movie movie = movieService.getMovieById(id);
        if (movie == null) return "redirect:/admin/movies";

        model.addAttribute("movie", movie);
        model.addAttribute("genres", genreRepository.findAll());
        return "admin/movie-form";
    }

    // 4. Xử lý lưu phim (Dùng cho cả Thêm và Sửa)
    @PostMapping("/save")
    public String saveMovie(@Valid @ModelAttribute("movie") Movie movie,
                            BindingResult bindingResult,
                            @RequestParam("genreName") String genreName,
                            HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        // Nếu có lỗi do nhập sai quy tắc (Tên trống, thời lượng ngắn...)
        if (bindingResult.hasErrors()) {
            return "admin/movie-form";
        }

        // LOGIC XỬ LÝ THỂ LOẠI THÔNG MINH
        if (genreName != null && !genreName.trim().isEmpty()) {
            String finalGenreName = genreName.trim();

            // Tìm trong Database xem thể loại này đã tồn tại chưa
            Genre genre = genreRepository.findByGenreNameIgnoreCase(finalGenreName)
                    .orElseGet(() -> {
                        // NẾU CHƯA CÓ: Tự động tạo thể loại mới tinh và lưu vào Database
                        Genre newGenre = new Genre();
                        newGenre.setGenreName(finalGenreName);
                        return genreRepository.save(newGenre);
                    });

            // Gắn thể loại (cũ hoặc vừa tạo mới) vào bộ phim
            movie.setGenre(genre);
        }

        // Cuối cùng, lưu phim vào DB
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