package com.yh9589.service;

import com.yh9589.data.UserRepository;
import com.yh9589.domain.Movie;
import com.yh9589.domain.User;
import com.yh9589.exception.BookmarkException;
import com.yh9589.exception.UsernameDuplicatedException;
import com.yh9589.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

/**
 * Created by beryh on 2017-02-15.
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<User> getLoggedInUser() {
        UserDetailsImpl principal = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return Optional.ofNullable(principal.getUser());
    }

    @Override
    public void refreshLoggedInUser(User user) {
        UserDetailsImpl principal = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        principal.setUser(user);
    }

    @Transactional
    @Override
    public User regist(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new UsernameDuplicatedException();
        }
    }

    @Transactional
    @Override
    public User save(User user) {
        return userRepository.saveAndFlush(user);
    }

    @Override
    public Set<Movie> getBookmark(User user) {
        user = userRepository.findOne(user.getId());
        refreshLoggedInUser(user);

        return user.getBookmarkList();
    }

    @Override
    public Set<Movie> addBookmark(User user, Movie movie) {
        if (!user.getBookmarkList().add(movie)) {
            throw new BookmarkException(BookmarkException.Action.PUT);
        }

        refreshLoggedInUser(userRepository.saveAndFlush(user));

        return user.getBookmarkList();
    }

    @Transactional
    @Override
    public Set<Movie> deleteBookmark(User user, Long movie_id) {
        if (!user.getBookmarkList().removeIf(m -> movie_id.equals(m.getId()))) {
            throw new BookmarkException(BookmarkException.Action.DELETE);
        }

        refreshLoggedInUser(userRepository.saveAndFlush(user));

        return user.getBookmarkList();
    }

    @Override
    public void bookmarkingMovieList(User user, Optional<Collection<Movie>> movieList) {
        movieList.ifPresent(m -> m.stream().filter(user.getBookmarkList()::contains).forEach(movie -> movie.setBookmarked(true)));
    }
}
