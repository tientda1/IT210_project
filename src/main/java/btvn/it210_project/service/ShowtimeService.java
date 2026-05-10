package btvn.it210_project.service;

import btvn.it210_project.model.entity.Movie;
import btvn.it210_project.model.entity.Showtime;
import btvn.it210_project.repository.MovieRepository;
import btvn.it210_project.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    // Cần gọi MovieRepository để tra cứu thời lượng phim
    private final MovieRepository movieRepository;

    // Lấy toàn bộ danh sách suất chiếu cho Admin quản lý
    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAll();
    }

    // CORE-05: Tạo suất chiếu mới và Check xung đột
    public boolean createShowtime(Showtime showtime) {
        // 1. Tìm bộ phim Admin vừa chọn để lấy thông tin Thời lượng (phút)
        Movie movie = movieRepository.findById(showtime.getMovie().getMovieId()).orElse(null);
        if (movie == null) return false;

        // 2. TỰ ĐỘNG TÍNH GIỜ KẾT THÚC = Giờ bắt đầu + Thời lượng phim
        LocalDateTime endTime = showtime.getStartTime().plusMinutes(movie.getDurationMinutes());
        showtime.setEndTime(endTime); // <-- DÒNG NÀY SẼ FIX TRIỆT ĐỂ LỖI NULL END_TIME

        // 3. Logic check xung đột thời gian
        // Khoảng thời gian phòng bị chiếm dụng = Từ lúc chiếu ĐẾN LÚC chiếu xong + 30 phút dọn dẹp
        LocalDateTime checkStart = showtime.getStartTime();
        LocalDateTime checkEnd = endTime.plusMinutes(30);

        List<Showtime> conflicts = showtimeRepository.findOverlappingShowtimes(
                showtime.getRoom().getRoomId(), checkStart, checkEnd);

        // Nếu có bất kỳ suất chiếu nào đè lên khoảng thời gian trên -> Báo lỗi (return false)
        if (!conflicts.isEmpty()) {
            return false;
        }

        // 4. Mọi thứ an toàn, lưu suất chiếu vào Database
        showtimeRepository.save(showtime);
        return true;
    }

    // CORE-08: Lấy danh sách suất chiếu khả dụng (Chưa chiếu) cho Khách hàng xem
    public List<Showtime> getAvailableShowtimes() {
        return showtimeRepository.findAll().stream()
                .filter(st -> st.getStartTime().isAfter(LocalDateTime.now()))
                .toList();
    }
}