package com.yh9589;

import com.yh9589.domain.Movie;
import com.yh9589.service.MovieService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by beryh on 2017-02-15.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KbtestApplicationTests {
    @Autowired
    WebApplicationContext webContext;

    private MockMvc mockMvc;
    private MockHttpSession session;

    @Test
    public void registration() throws Exception {

        mockMvc.perform(post("/regist")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "test")
                .param("password", "test")
                .param("passwordConfirm", "test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/"));
    }

    @Before
    public void setupMockMvc() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webContext)
                .apply(springSecurity())
                .build();

        MockitoAnnotations.initMocks(this);

        session = new MockHttpSession();
    }

    @Test
    @WithUserDetails(value="test", userDetailsServiceBeanName = "userDetailsServiceImpl")
    public void GetMovieListWithMockUser() throws Exception {
        List<Movie> dummyList = new ArrayList<>();
        Movie movie = new Movie();
        movie.setTitle("Test");
        dummyList.add(movie);

        mockMvc.perform(get("/movie/search?query=곡성"))
                .andDo(print())
                .andExpect(model().attributeExists("movieList"))
                .andExpect(model().attribute("movieList", hasSize(1)));
    }

    @Test
    @WithUserDetails(value="test", userDetailsServiceBeanName = "userDetailsServiceImpl")
    public void AddWeirdBookmarkWithMockUser() throws Exception {
        List<Movie> dummyList = new ArrayList<>();
        Movie movie = new Movie();
        movie.setId(74000L);
        movie.setTitle("Test");
        dummyList.add(movie);
        MovieService.SearchResult dummySearchResult = new MovieService.SearchResult(dummyList, 0, 5, 1);

        session.setAttribute("searchResult", dummySearchResult);

        mockMvc.perform(put("/bookmark?movie_id=1")
                .with(csrf())
                .session((MockHttpSession) session))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(status().reason(containsString("Bookmarking Failed")));
    }

    @Test
    @WithUserDetails(value="test", userDetailsServiceBeanName = "userDetailsServiceImpl")
    public void AddAndDeleteBookmarkWithMockUser() throws Exception {
        List<Movie> dummyList = new ArrayList<>();
        Movie movie = new Movie();
        movie.setId(84000L);
        movie.setTitle("Test");
        dummyList.add(movie);
        MovieService.SearchResult dummySearchResult = new MovieService.SearchResult(dummyList, 0, 5, 1);

        session.setAttribute("searchResult", dummySearchResult);

        mockMvc.perform(put("/bookmark?movie_id=100")
                .with(csrf())
                .session(session))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookmark"));

        mockMvc.perform(delete("/bookmark?movie_id=100")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookmark"));

    }

    @Test
    @WithUserDetails(value="test", userDetailsServiceBeanName = "userDetailsServiceImpl")
    public void MockGetMovieDetails() throws Exception {
        HttpSession session = mockMvc.perform(get("/movie/search?query=곡성"))
                .andDo(print())
                .andReturn()
                .getRequest()
                .getSession();

        mockMvc.perform(get("/movie?id=84000")
                .session((MockHttpSession) session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("movie"));
    }

    @Test
    @WithUserDetails(value="test", userDetailsServiceBeanName = "userDetailsServiceImpl")
    public void MockGetMovieDetailsFailed() throws Exception {
        mockMvc.perform(get("/movie?id=0")
                .session(session))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(status().reason(containsString("Cannot find movie")));
    }
}
