package btvn.it210_project.repository;

import btvn.it210_project.model.entity.Movie;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends CrudRepository<Movie, Integer> {
    @Override
    List<Movie> findAll();
}
