package com.yh9589;

import com.yh9589.controller.MovieController;
import com.yh9589.domain.Movie;
import com.yh9589.service.MovieService;
import com.yh9589.service.UserServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Created by beryh on 2017-02-15.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MovieControllerTest {
    private MockMvc mockMvc;
    private MockHttpSession session;

    @Mock
    private MovieService movieService;

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private MovieController movieController;

    @Before
    public void setupMockMvc() {
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(movieController).build();
        session = new MockHttpSession();
    }

    @Test
    @WithUserDetails(value="test", userDetailsServiceBeanName = "userDetailsServiceImpl")
    public void MockMovieList() throws Exception {
        List<Movie> dummyList = new ArrayList<>();
        Movie movie = new Movie();
        dummyList.add(movie);

        MovieService.SearchResult dummySearchResult = new MovieService.SearchResult(dummyList, 0, 5, 1);
        when(movieService.getMovieListByQuery(any(String.class), any(Integer.class), any(Integer.class))).thenReturn(dummySearchResult);

        mockMvc.perform(get("/movie/search?query=test"))
                .andExpect(status().isOk())
                .andExpect(view().name("movie_main"))
                .andExpect(model().attribute("movieList", samePropertyValuesAs(dummySearchResult.getMovieList())));
    }

    @Test
    @WithUserDetails(value="test", userDetailsServiceBeanName = "userDetailsServiceImpl")
    public void MockGetMovieDetails() throws Exception {
        List<Movie> dummyList = new ArrayList<>();
        Movie movie = new Movie();
        movie.setId(1L);
        dummyList.add(movie);

        MovieService.SearchResult dummySearchResult = new MovieService.SearchResult(dummyList, 0, 5, 1);
        session.setAttribute("searchResult", dummySearchResult);

        mockMvc.perform(get("/movie?id=1")
                .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("movie_detail"))
                .andExpect(model().attributeExists("movie"))
                .andExpect(model().attribute("movie", movie));
    }
}
