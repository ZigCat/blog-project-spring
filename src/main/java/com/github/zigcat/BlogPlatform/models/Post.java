package com.github.zigcat.BlogPlatform.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.zigcat.BlogPlatform.services.LocalDateToStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(exclude = {"comments"})
@EqualsAndHashCode
@NoArgsConstructor
public class Post {
    @Id
    @SequenceGenerator(
            name = "post_sequence",
            sequenceName = "post_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "post_sequence"
    )
    private Integer id;
    private String content;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;
    @Convert(converter = LocalDateToStringConverter.class)
    private LocalDate creationDate;
    @JsonIgnore
    @OneToMany(mappedBy = "post")
    private List<Comment> comments;

    public Post(String content, AppUser user, LocalDate creationDate) {
        this.content = content;
        this.user = user;
        this.creationDate = creationDate;
    }
}
