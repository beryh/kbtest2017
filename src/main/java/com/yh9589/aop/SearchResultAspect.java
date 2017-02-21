package com.yh9589.aop;

import com.yh9589.domain.Movie;
import com.yh9589.service.UserService;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.*;

/**
 * Created by beryh on 2017-02-18.
 */
@Aspect
@Component
public class SearchResultAspect {
    @Autowired
    private UserService userService;

    @Pointcut("execution(* com.yh9589.controller.MovieController.search(..)) && args(*,order,..,model,*)")
    private void searchInMC(Optional<String> order, Model model) {}

    @Pointcut("execution(* com.yh9589.controller.UserController.getBookmarks(..)) && args(*,order,..,model,*)")
    private void getBookmarksInUC(Optional<String> order, Model model) {}

    @Pointcut("execution(* com.yh9589.controller.UserController.*Bookmark(..)) && args(..,model,*)")
    private void allBookmarkWorksInUC(Model model) {}

    @After("searchInMC(order, model) || getBookmarksInUC(order, model)")
    private void orderMovieList(Optional<String> order, Model model) {
        Object movieListObj = model.asMap().get("movieList");
        if(movieListObj == null) return;

        if (List.class.isAssignableFrom(movieListObj.getClass())) {
            List<Movie> movieList =(List) movieListObj;

            try {
                switch (order.orElse("none").toLowerCase()) {
                    case "year":
                        ((List) movieList).sort(Comparator.comparing(Movie::getYear));
                        break;
                    case "title":
                        ((List) movieList).sort(Comparator.comparing(Movie::getTitle));
                        break;
                    case "rating":
                        ((List) movieList).sort(Comparator.comparing(Movie::getRating).reversed());
                        break;
                    default:
                        break;
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

            userService.getLoggedInUser().ifPresent(user -> userService.bookmarkingMovieList(user, Optional.ofNullable(movieList)));
        }
    }
}
