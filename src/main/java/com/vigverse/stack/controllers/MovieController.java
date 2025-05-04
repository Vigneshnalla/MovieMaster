package com.vigverse.stack.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vigverse.stack.dto.MovieDto;
import com.vigverse.stack.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movie")
public class MovieController {

    @Autowired
    private MovieService movieService;


    @PostMapping("/add-movie")
    public ResponseEntity<MovieDto> addMoiveHandler(@RequestPart("file")MultipartFile file,
                                                    @RequestPart String movieDto) throws IOException {
        MovieDto dto = convertToMovieDto(movieDto);
        return new ResponseEntity<>(movieService.addMovie(dto,file), HttpStatus.CREATED);
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieDto> getMovieHandler(@PathVariable Integer movieId) throws IOException {
        return ResponseEntity.ok(movieService.getMovie(movieId));
    }
    @GetMapping("/all")
    public ResponseEntity<List<MovieDto>> getAllMoviesHandler() throws IOException {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @PutMapping("/update/{movieId}")
    public ResponseEntity<MovieDto> updateMovieHandler(@PathVariable Integer movieId, @RequestPart("file") MultipartFile multipartFile,
                                                       @RequestPart String movieDto ) throws IOException {
        MovieDto dto = convertToMovieDto(movieDto);
        if(multipartFile.isEmpty()) multipartFile = null;
        return ResponseEntity.ok(movieService.updateMovie(movieId,dto,multipartFile));
    }

    @DeleteMapping("/delete/{movieId}")
    public ResponseEntity<String> updateMovieHandler(@PathVariable Integer movieId) throws IOException {
        return ResponseEntity.ok(movieService.deleteMovie(movieId));
    }


    private MovieDto convertToMovieDto(String movieDtoObj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(movieDtoObj , MovieDto.class);

    }
}
