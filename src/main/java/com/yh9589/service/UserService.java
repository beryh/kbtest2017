package com.yh9589.service;

import com.yh9589.domain.Movie;
import com.yh9589.domain.User;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Created by beryh on 2017-02-15.
 */
public interface UserService {
    Optional<User> getLoggedInUser();
    void refreshLoggedInUser(User user);

    User save(User user);
    User regist(User user);
    Set<Movie> getBookmark(User user);
    Set<Movie> addBookmark(User user, Movie movie);
    Set<Movie> deleteBookmark(User user, Long movie_id);
    void bookmarkingMovieList(User user, Optional<Collection<Movie>> movieList);
}
