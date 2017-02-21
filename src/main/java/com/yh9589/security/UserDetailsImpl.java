package com.yh9589.security;

import com.yh9589.domain.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by beryh on 2017-02-21.
 */

public class UserDetailsImpl extends org.springframework.security.core.userdetails.User {
    @Getter
    @Setter
    private User user;

    private static List<GrantedAuthority> getUserAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return authorities;
    }

    public UserDetailsImpl(User user) {
        super(user.getUsername(), user.getPassword(), getUserAuthorities());

        this.user = user;
    }
}
