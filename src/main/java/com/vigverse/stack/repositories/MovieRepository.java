package com.vigverse.stack.repositories;

import com.vigverse.stack.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie,Integer> {
}
