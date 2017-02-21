package com.yh9589.domain;

import lombok.*;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.*;

/**
 * Created by beryh on 2017-02-15.
 */
@Data
@Entity
@EqualsAndHashCode( exclude = {"password", "bookmarkList"}, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    @NonNull
    private String username;

    @Column
    @NonNull
    private String password;

    @ManyToMany
    @NonNull
    @Cascade(value=org.hibernate.annotations.CascadeType.DELETE)
    @JoinTable(
            name="bookmark_lists",
            joinColumns = @JoinColumn(name="user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name="movie_id", referencedColumnName = "id")
    )
    Set<Movie> bookmarkList;
}
