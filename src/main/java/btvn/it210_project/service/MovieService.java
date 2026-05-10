package btvn.it210_project.service;

import btvn.it210_project.model.entity.Movie;
import btvn.it210_project.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    // Lấy danh sách toàn bộ phim
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    // Lấy thông tin 1 bộ phim theo ID
    public Movie getMovieById(Integer id) {
        return movieRepository.findById(id).orElse(null);
    }

    // Thêm mới hoặc Cập nhật phim (JPA tự động phân biệt nhờ ID)
    public void saveMovie(Movie movie) {
        movieRepository.save(movie);
    }

    // Xóa phim
    public void deleteMovie(Integer id) {
        movieRepository.deleteById(id);
    }
}