package com.vigverse.stack.service;

import com.vigverse.stack.dto.MovieDto;
import com.vigverse.stack.dto.MoviePageResponse;
import com.vigverse.stack.entities.Movie;
import com.vigverse.stack.exceptions.MovieNotFoundException;
import com.vigverse.stack.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        System.out.println("add movie");
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
        Movie movie = movieRepository.findById(movieId).orElseThrow(()-> new MovieNotFoundException("Movie Not Found"));
        String posterUrl = baseUrl + "/file/"+ movie.getPoster();
        MovieDto movieDto = mapMovieToMovieDto(movie, posterUrl);
        return movieDto;
    }

    @Override
    public List<MovieDto> getAllMovies() {
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
        Movie movie = movieRepository.findById(movieId).orElseThrow(()-> new MovieNotFoundException("Movie Not found with given id"));


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
        Movie movie = movieRepository.findById(id).orElseThrow(()-> new MovieNotFoundException("Movie Not found with given id"));
        Files.deleteIfExists(Paths.get(path + File.separator + movie.getPoster()));
        movieRepository.deleteById(id);
        return "Movie Deleted with Id : "+ movie.getMovieId();
    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber,pageSize);
        Page<Movie> moviePages=movieRepository.findAll(pageable);
        List<Movie> moviesList = moviePages.getContent();
        List<MovieDto> movieDtos = moviesList.stream()
                .map(movie -> {
                    String posterUrl = baseUrl + "/file/" + movie.getPoster();
                    return mapMovieToMovieDto(movie, posterUrl);
                })
                .collect(Collectors.toList());


        return new MoviePageResponse(movieDtos,pageNumber,pageSize,
                (int) moviePages.getTotalElements(),moviePages.getTotalPages(),moviePages.isLast());
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String dir) {
        Sort sort = dir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Movie> moviePages = movieRepository.findAll(pageable);

        List<MovieDto> movieDtos = moviePages.getContent().stream()
                .map(movie -> {
                    String posterUrl = baseUrl + "/file/" + movie.getPoster();
                    return mapMovieToMovieDto(movie, posterUrl);
                })
                .collect(Collectors.toList());

        return new MoviePageResponse(
                movieDtos,
                pageNumber,
                pageSize,
                (int) moviePages.getTotalElements(),
                moviePages.getTotalPages(),
                moviePages.isLast()
        );
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
