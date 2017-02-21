package com.yh9589.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * Created by beryh on 2017-02-15.
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Movie {
    @Id
    private Long id;

    @Column
    private String title;

    @Column
    private String thumbnail;

    @Lob
    @Column
    @ElementCollection(targetClass=String.class)
    private List<String> story;

    @Column
    @ElementCollection(targetClass=String.class)
    private List<String> actor;

    @Column
    @ElementCollection(targetClass=String.class)
    private List<String> director;

    @Column
    @ElementCollection(targetClass=String.class)
    private List<String> photo;

    @Column
    @ElementCollection(targetClass=String.class)
    private List<String> genre;

    @Column
    private Integer year;

    @Column
    private String link;

    @Column
    private Float rating;

    @Transient
    private Boolean bookmarked = false;

    @Override
    public boolean equals(Object that) {
        if (that instanceof Movie) {
            return this.getId().equals(((Movie) that).getId()) && this.getTitle().equals(((Movie) that).getTitle());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }
}
