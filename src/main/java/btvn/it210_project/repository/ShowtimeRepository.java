package btvn.it210_project.repository;

import btvn.it210_project.model.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Integer> {

    // CORE-05: Tìm các suất chiếu trùng lặp thời gian trong cùng một phòng (bao gồm cả thời gian dọn dẹp)
    @Query("SELECT s FROM Showtime s WHERE s.room.roomId = :roomId " +
            "AND s.startTime < :checkEnd " +
            "AND s.endTime > :checkStart")
    List<Showtime> findOverlappingShowtimes(@Param("roomId") Integer roomId,
                                            @Param("checkStart") LocalDateTime checkStart,
                                            @Param("checkEnd") LocalDateTime checkEnd);

    // Tìm các suất chiếu của 1 phim cụ thể, diễn ra sau thời điểm hiện tại, sắp xếp theo giờ tăng dần
    List<Showtime> findByMovie_MovieIdAndStartTimeAfterOrderByStartTimeAsc(Integer movieId, LocalDateTime time);
}