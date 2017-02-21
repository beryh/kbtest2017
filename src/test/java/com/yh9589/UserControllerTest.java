package com.yh9589;

import com.yh9589.controller.UserController;
import com.yh9589.domain.User;
import com.yh9589.service.DaumMovieServiceImpl;
import com.yh9589.service.UserServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by beryh on 2017-02-14.
 */
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
    private MockMvc mockMvc;
    private MockHttpSession session;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private DaumMovieServiceImpl movieService;

    @InjectMocks
    private UserController userController;

    @Autowired
    FilterChainProxy springSecurityFilterChain;


    @Before
    public void setupMockMvc() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).apply(springSecurity(springSecurityFilterChain)).build();

        session = new MockHttpSession();
    }

    @Test
    public void registration() throws Exception {
        User dummyUser = new User( "testuser", "test", new HashSet<>());
        when(userService.regist(any(User.class))).thenReturn(dummyUser);

        mockMvc.perform(post("/regist")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", "testuser")
            .param("password", "test")
            .param("passwordConfirm", "test"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", "/"));
    }
}
