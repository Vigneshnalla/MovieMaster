package com.vigverse.stack.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MovieDto {
    private Integer movieId;

    @NotBlank(message = "Please Provide movie title")
    private String title;

    @NotBlank(message = "Please Provide movie's director name !")
    private String director;


    private String studio;

    private Set<String> movieCast;

    @NotBlank(message = "Please Provide movie's releaseYear !")
    private Integer releaseYear;

    private String poster;

    @NotBlank(message = "Please Provide poster url !")
    private String posterUrl;


}


