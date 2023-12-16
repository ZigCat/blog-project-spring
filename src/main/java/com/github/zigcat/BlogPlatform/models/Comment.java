package com.github.zigcat.BlogPlatform.models;

import com.github.zigcat.BlogPlatform.services.LocalDateToStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class Comment {
    @Id
    @SequenceGenerator(
            name = "comment_sequence",
            sequenceName = "comment_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "comment_sequence"
    )
    private Integer id;
    private String content;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;
    @Convert(converter = LocalDateToStringConverter.class)
    private LocalDate creationDate;

    public Comment(String content, AppUser user, Post post, LocalDate creationDate) {
        this.content = content;
        this.user = user;
        this.post = post;
        this.creationDate = creationDate;
    }
}
