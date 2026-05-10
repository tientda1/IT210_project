package btvn.it210_project.repository;

import btvn.it210_project.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    // CORE-06: Kiểm tra xem một ghế cụ thể trong suất chiếu này đã có ai đặt thành công chưa
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Ticket t " +
            "WHERE t.showtime.showtimeId = :showtimeId AND t.seat.seatId = :seatId " +
            "AND t.booking.status = 'CONFIRMED'")
    boolean existsByShowtimeAndSeatId(@Param("showtimeId") Integer showtimeId,
                                      @Param("seatId") Integer seatId);
    // Lấy danh sách ID của các ghế ĐÃ BỊ MUA trong 1 suất chiếu
    @Query("SELECT t.seat.seatId FROM Ticket t WHERE t.showtime.showtimeId = :showtimeId AND t.booking.status = 'CONFIRMED'")
    List<Integer> findBookedSeatIdsByShowtime(@Param("showtimeId") Integer showtimeId);
}