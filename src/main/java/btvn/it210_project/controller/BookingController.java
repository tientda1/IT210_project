package btvn.it210_project.controller;

import btvn.it210_project.model.entity.Movie;
import btvn.it210_project.model.entity.Seat;
import btvn.it210_project.model.entity.Showtime;
import btvn.it210_project.model.entity.User;
import btvn.it210_project.repository.SeatRepository;
import btvn.it210_project.repository.ShowtimeRepository;
import btvn.it210_project.repository.TicketRepository;

import btvn.it210_project.service.MovieService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final MovieService movieService;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final btvn.it210_project.service.BookingService bookingService;

    // 1. Khách bấm "ĐẶT VÉ NGAY" -> Trả về trang chọn Ngày/Giờ chiếu
    @GetMapping("/movie/{movieId}")
    public String selectShowtime(@PathVariable Integer movieId, Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";

        Movie movie = movieService.getMovieById(movieId);
        // Chỉ lấy các suất chiếu chưa bắt đầu
        List<Showtime> showtimes = showtimeRepository.findByMovie_MovieIdAndStartTimeAfterOrderByStartTimeAsc(movieId, LocalDateTime.now());

        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", showtimes);
        return "customer/showtime-selection";
    }

    // 2. Khách chọn xong Giờ chiếu -> Trả về Sơ đồ ghế ngồi
    @GetMapping("/showtime/{showtimeId}")
    public String selectSeats(@PathVariable Integer showtimeId, Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";

        Showtime showtime = showtimeRepository.findById(showtimeId).orElseThrow();
        // Lấy toàn bộ ghế của phòng đó
        List<Seat> allSeats = seatRepository.findByRoom_RoomId(showtime.getRoom().getRoomId());
        // Lấy danh sách các ghế đã bị người khác mua
        List<Integer> bookedSeatIds = ticketRepository.findBookedSeatIdsByShowtime(showtimeId);

        model.addAttribute("showtime", showtime);
        model.addAttribute("allSeats", allSeats);
        model.addAttribute("bookedSeatIds", bookedSeatIds);

        return "customer/seat-selection"; // Giao diện này ta sẽ code sau
    }
    // 3. Khách bấm XÁC NHẬN -> Xử lý lưu Database
    @PostMapping("/confirm")
    public String confirmBooking(@RequestParam Integer showtimeId,
                                 @RequestParam String seatIds,
                                 HttpSession session,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        // Chuyển chuỗi "1,2,3" thành danh sách List<Integer>
        List<Integer> listSeatIds = java.util.Arrays.stream(seatIds.split(","))
                .map(Integer::parseInt).toList();

        try {
            // Gọi Service để xử lý (Sử dụng Transactional ở bước sau)
            bookingService.processBooking(user.getId(), showtimeId, listSeatIds);
            ra.addFlashAttribute("success", "Đặt vé thành công! Chúc bạn xem phim vui vẻ.");
            return "redirect:/history"; // Chuyển về trang lịch sử để xem vé vừa đặt
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: Một trong số các ghế bạn chọn vừa bị người khác mua mất!");
            return "redirect:/booking/showtime/" + showtimeId;
        }
    }
    // 4. Khách bấm HỦY VÉ
    @GetMapping("/cancel/{bookingId}")
    public String cancelBooking(@PathVariable Integer bookingId, HttpSession session,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        btvn.it210_project.model.entity.User user = (btvn.it210_project.model.entity.User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        String result = bookingService.cancelBooking(bookingId, user.getId());
        if ("OK".equals(result)) {
            ra.addFlashAttribute("success", "Hủy vé thành công! Sơ đồ ghế đã được giải phóng.");
        } else {
            ra.addFlashAttribute("error", result);
        }
        return "redirect:/history";
    }
}