package com.yh9589.controller;

import com.yh9589.domain.Movie;
import com.yh9589.domain.User;
import com.yh9589.exception.BookmarkException;
import com.yh9589.exception.UsernameDuplicatedException;
import com.yh9589.security.UserDetailsImpl;
import com.yh9589.service.MovieService;
import com.yh9589.service.UserService;
import com.yh9589.service.form.UserRegisterForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by beryh on 2017-02-15.
 */
@Controller
public class UserController {
    private UserService userService;
    private MovieService movieService;

    static final Integer DEFAULT_PAGE_VALUE = 1;
    static final Integer DEFAULT_COUNT_VALUE = 5;

    @Autowired
    public UserController(UserService userService, MovieService movieService) {
        this.userService = userService;
        this.movieService = movieService;
    }

    @RequestMapping(value="/regist", method= RequestMethod.GET)
    public String showRegistrationForm(Model model) {
        model.addAttribute(new UserRegisterForm());
        return "registerForm";
    }

    @RequestMapping(value="/regist", method=RequestMethod.POST)
    public String processRegistration(@Valid UserRegisterForm registerForm, Errors errors, RedirectAttributes model) {
        if (errors.hasErrors()) {
            return "registerForm";
        } else if (!registerForm.checkPasswordValidation()) {
            errors.rejectValue("password", "error.registration", "Password is not Confirmed");
            return "registerForm";
        }

        User user = new User(registerForm.getUsername(), registerForm.getPassword(), new HashSet<>());

        try {
            UserDetailsImpl userDetails = new UserDetailsImpl(userService.regist(user));

            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (UsernameDuplicatedException e) {
            errors.rejectValue("username", "error.registration", "Username is duplicate");
            return "registerForm";
        }

        return "redirect:/";
    }

    @RequestMapping(value="/bookmark", method=RequestMethod.GET)
    public String getBookmarks(@AuthenticationPrincipal UserDetailsImpl principal, @RequestParam("order") Optional<String> order, @RequestParam("page") Optional<Integer> page, @RequestParam("count") Optional<Integer> count, Model model, HttpSession session) {
        if (!Optional.ofNullable(principal).isPresent()) {
            return "redirect:/";
        }
        // FETCH BOOKMARK LIST
        List<Movie> bookmarkList = new ArrayList<>(userService.getBookmark(principal.getUser()));
        //List<Movie> bookmarkList = new ArrayList<>(userService.getBookmark(userService.getLoggedInUser().orElseThrow(() -> new UnauthorizedRequestException())));

        // Filter by Genre
        model.addAttribute(bookmarkList.stream().skip(count.orElse(DEFAULT_COUNT_VALUE) * (page.orElse(DEFAULT_PAGE_VALUE) - 1)).limit(count.orElse(DEFAULT_COUNT_VALUE)).collect(Collectors.toList()));
        model.addAttribute("count", count.orElse(DEFAULT_COUNT_VALUE));
        model.addAttribute("page", page.orElse(DEFAULT_PAGE_VALUE));
        model.addAttribute("nextable", bookmarkList.size() > page.orElse(DEFAULT_PAGE_VALUE) * count.orElse(DEFAULT_COUNT_VALUE));
        model.addAttribute("listedBy", "bookmark");
        return "movie_main";
    }

    @RequestMapping(value="/bookmark", method=RequestMethod.PUT)
    public String addBookmark(@AuthenticationPrincipal UserDetailsImpl principal, @RequestParam("movie_id") final Long movie_id, Model model, HttpSession session) throws BookmarkException {
        // Get Authorized UserDetail

        // CHECK SESSION
        // PROTECTION FOR SESSION IS NULL
        List<Movie> movieListInSession = Optional.ofNullable((MovieService.SearchResult) session.getAttribute("searchResult")).orElse(new MovieService.SearchResult()).getMovieList();

        // 2. 검색 결과가 null
        Optional<Movie> movie = movieListInSession.stream().filter(m -> movie_id.equals(m.getId())).findFirst();

        // CHECK REPOSITORY
        Movie target = movie.orElseGet(
                () -> movieService.findOne(movie_id).orElseThrow(() -> new BookmarkException(BookmarkException.Action.PUT))
        );

        // SAVE MOVIE DETAIL
        movieService.save(target);

        userService.addBookmark(principal.getUser(), target);
        return "redirect:/bookmark";
    }

    @RequestMapping(value="/bookmark", method=RequestMethod.DELETE)
    public String deleteBookmark(@RequestParam("movie_id") final Long movie_id, Model model, HttpSession session) throws BookmarkException {
        userService.getLoggedInUser().ifPresent(user -> user.setBookmarkList(userService.deleteBookmark(user, movie_id)));
        return "redirect:/bookmark";
    }

    @ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Bookmarking Failed")
    @ExceptionHandler(BookmarkException.class)
    public String bookmarkExceptionHandler(BookmarkException e) {
        return "Bookmarking Failed";
    }
}
