package com.yh9589.controller;

import com.yh9589.domain.Movie;
import com.yh9589.exception.MovieNotFoundException;
import com.yh9589.service.MovieService;
import com.yh9589.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

/**
 * Created by beryh on 2017-02-15.
 */
@RequestMapping(value="/movie")
@Controller
public class MovieController {
    private MovieService movieService;
    private UserService userService;

    static final Integer DEFAULT_PAGE_VALUE = 1;
    static final Integer DEFAULT_COUNT_VALUE = 5;

    @Autowired
    public MovieController(MovieService movieService, UserService userService) {
        this.movieService = movieService;
        this.userService = userService;
    }

    @RequestMapping(value="search", method=RequestMethod.GET)
    public String search(@RequestParam("query") String query, @RequestParam("order") Optional<String> order, @RequestParam("page") Optional<Integer> page, @RequestParam("count") Optional<Integer> count, Model model, HttpSession session) {
        MovieService.SearchResult searchResult = movieService.getMovieListByQuery(query, page.orElse(DEFAULT_PAGE_VALUE), count.orElse(DEFAULT_COUNT_VALUE));

        session.setAttribute("searchResult", searchResult);

        model.addAttribute(searchResult.getMovieList());
        model.addAttribute("count", searchResult.getCount());
        model.addAttribute("page", page.orElse(DEFAULT_PAGE_VALUE));
        model.addAttribute("nextable", searchResult.getTotalCount() > page.orElse(1) * count.orElse(DEFAULT_COUNT_VALUE));
        model.addAttribute("listedBy", "query");
        return "movie_main";
    }

    @RequestMapping(method=RequestMethod.GET)
    public String getMovieDetails(@RequestParam("id") Long id, Model model, HttpSession session) {
        // CHECK SESSION
        // PROTECTION FOR SESSION IS NULL
        List<Movie> movieListInSession = Optional.ofNullable((MovieService.SearchResult) session.getAttribute("searchResult")).orElse(new MovieService.SearchResult()).getMovieList();

        // 2. 검색 결과가 null
        Optional<Movie> movie = movieListInSession.stream().filter(m -> id.equals(m.getId())).findFirst();

        // CHECK REPOSITORY
        Movie target = movie.orElseGet(
                () -> movieService.findOne(id).orElseThrow(() -> new MovieNotFoundException()
        ));

        model.addAttribute(target);
        return "movie_detail";
    }
}
