package btvn.it210_project.repository;

import btvn.it210_project.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // CORE-07: Lấy lịch sử hóa đơn kèm theo Vé, Suất chiếu, Phim (Dùng FETCH để tránh lỗi N+1 và tăng tốc DB)
    @Query("SELECT DISTINCT b FROM Booking b " +
            "JOIN FETCH b.tickets t " +
            "JOIN FETCH t.showtime s " +
            "JOIN FETCH s.movie m " +
            "WHERE b.user.id = :userId ORDER BY b.bookingDate DESC")
    List<Booking> findDetailedHistoryByUserId(@Param("userId") Integer userId);
}