package com.yh9589.service;

import com.yh9589.domain.Movie;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by beryh on 2017-02-15.
 */
public interface MovieService {
    SearchResult getMovieListByQuery(String query, Integer page, Integer count);
    void save(Movie movie);
    Optional<Movie> findOne(Long id);

    @AllArgsConstructor
    @Setter
    @Getter
    class SearchResult {
        List<Movie> movieList;
        Integer totalCount = 0;
        Integer count = 5;
        Integer page = 1;

        public SearchResult() {
            movieList = new ArrayList<>();
        }
    }
}
