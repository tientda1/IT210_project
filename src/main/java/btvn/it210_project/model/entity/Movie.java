package btvn.it210_project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "movies")
@Getter
@Setter
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer movieId;
    @NotBlank(message = "Tên phim không được để trống!")
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @NotNull(message = "Thời lượng không được để trống!")
    @Min(value = 30, message = "Thời lượng phim phải từ 30 phút trở lên!")
    private Integer durationMinutes;
    @NotNull(message = "Ngày khởi chiếu không được để trống!")
    private LocalDate releaseDate;
    @NotBlank(message = "Tên đạo diễn không được để trống!")
    private String director;
    private String posterUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id")
    private Genre genre;
}