package btvn.it210_project.service;

import btvn.it210_project.model.entity.*;
import btvn.it210_project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    @Transactional // QUAN TRỌNG: Đảm bảo Rollback nếu có lỗi giữa chừng
    public void processBooking(Integer userId, Integer showtimeId, List<Integer> seatIds) {
        User user = userRepository.findById(userId).orElseThrow();
        Showtime showtime = showtimeRepository.findById(showtimeId).orElseThrow();

        // 1. Kiểm tra lại một lần nữa xem có ghế nào vừa bị mua trong tích tắc đó không
        for (Integer sId : seatIds) {
            if (ticketRepository.existsByShowtimeAndSeatId(showtimeId, sId)) {
                throw new RuntimeException("Ghế đã bị người khác chiếm!");
            }
        }

        // 2. Tạo hóa đơn (Booking)
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus("CONFIRMED");
        Booking savedBooking = bookingRepository.save(booking);

        // 3. Tạo các vé chi tiết (Tickets)
        for (Integer sId : seatIds) {
            Seat seat = seatRepository.findById(sId).orElseThrow();
            Ticket ticket = new Ticket();
            ticket.setBooking(savedBooking);
            ticket.setShowtime(showtime);
            ticket.setSeat(seat);
            ticketRepository.save(ticket);
        }
    }

    // Lấy lịch sử đặt vé cho khách hàng
    public List<Booking> getCustomerHistory(Integer userId) {
        return bookingRepository.findDetailedHistoryByUserId(userId);
    }
    // CORE-09: Hủy vé trước 24h và Giải phóng ghế
    @Transactional
    public String cancelBooking(Integer bookingId, Integer userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Kiểm tra bảo mật: Hóa đơn này có đúng là của ông đang đăng nhập không?
        if (!booking.getUser().getId().equals(userId)) {
            return "Bạn không có quyền hủy vé này!";
        }

        if ("CANCELLED".equals(booking.getStatus())) {
            return "Vé này đã được hủy trước đó rồi!";
        }

        if (booking.getTickets().isEmpty()) return "Hóa đơn lỗi (không có vé)";

        // Lấy thông tin suất chiếu từ vé đầu tiên
        Showtime st = booking.getTickets().get(0).getShowtime();

        // Kiểm tra luật 24h: Nếu (Bây giờ + 24 tiếng) mà đã vượt quá Giờ chiếu -> Tức là còn quá ít thời gian
        if (LocalDateTime.now().plusHours(24).isAfter(st.getStartTime())) {
            return "Chỉ có thể hủy vé trước giờ chiếu ít nhất 24 tiếng!";
        }

        // Hủy vé: Cập nhật trạng thái thành CANCELLED
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        // Nhờ câu lệnh @Query trong TicketRepository của chúng ta chỉ lọc các vé 'CONFIRMED',
        // nên khi chuyển sang 'CANCELLED', các ghế này sẽ tự động trống trở lại trên sơ đồ!
        return "OK";
    }
}