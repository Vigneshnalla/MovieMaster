package com.vigverse.stack.service;

import com.vigverse.stack.dto.MovieDto;
import com.vigverse.stack.entities.Movie;
import com.vigverse.stack.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService{

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private FileService fileService;

    @Value("${project.poster}")
    private String path;


    @Value("${base.url}")
    private String baseUrl;

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {
        // upload the file
        String uploadedFileName = fileService.uploadFile(path,file);

        // set the value of field poster as filename
        movieDto.setPoster(uploadedFileName);

        // map dto to movie object
        Movie movie = mapMovieDtoToMovie(movieDto);

        // save the movie object
        Movie savedMovie= movieRepository.save(movie);

        //generate the poster url
        String posterUrl = baseUrl + "/file/" + uploadedFileName;

        //map movieDto object to dto object
        MovieDto responseDto= mapMovieToMovieDto(savedMovie,posterUrl);
        return responseDto;
    }

    @Override
    public MovieDto getMovie(Integer movieId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow(()-> new RuntimeException("Movie Not Found"));
        String posterUrl = baseUrl + "/file/"+ movie.getPoster();
        MovieDto movieDto = mapMovieToMovieDto(movie, posterUrl);
        return movieDto;
    }

    @Override
    public List<MovieDto> getAllMovies() {
        String baseUrl = "http://localhost:8080"; // Or dynamically fetched

        List<Movie> movies = movieRepository.findAll();
        List<MovieDto> movieDtos = movies.stream()
                .map(movie -> {
                    String posterUrl = baseUrl + "/file/" + movie.getPoster();
                    return mapMovieToMovieDto(movie, posterUrl);
                })
                .collect(Collectors.toList());

        return movieDtos;
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {
        Movie movie = movieRepository.findById(movieId).orElseThrow(()-> new RuntimeException("Movie Not found with given id"));


        if(file != null){
            String fileName = movie.getPoster();
            Path filePath = Paths.get(path + File.separator + fileName);
            if (Files.exists(filePath)) {
                try {
                    Files.delete(filePath);
                } catch (IOException e) {
                    throw new RuntimeException("Error deleting old file: " + e.getMessage());
                }
            }
            String newlyUploadedFileName = fileService.uploadFile(path, file);
            movieDto.setPoster(newlyUploadedFileName);
        }
        Movie updatedMovie = mapMovieDtoToMovie(movieDto);
        Movie savedMovie = movieRepository.save(updatedMovie);
        String posterUrl = baseUrl + "/file/" + savedMovie.getPoster();
        return mapMovieToMovieDto(savedMovie, posterUrl);
    }

    @Override
    public String deleteMovie(Integer id) throws IOException {
        Movie movie = movieRepository.findById(id).orElseThrow(()-> new RuntimeException("Movie Not found with given id"));
        Files.deleteIfExists(Paths.get(path + File.separator + movie.getPoster()));
        movieRepository.deleteById(id);
        return "Movie Deleted with Id : "+ movie.getMovieId();
    }

    public Movie mapMovieDtoToMovie(MovieDto movieDto){
        Movie movie = new Movie(
                movieDto.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );
        return  movie;
    }


    public MovieDto mapMovieToMovieDto(Movie movie, String posterUrl){
        MovieDto movieDto = new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );
        return  movieDto;

    }
}
