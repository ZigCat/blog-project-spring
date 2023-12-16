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
@ToString(exclude = {"password", "posts", "comments"})
@EqualsAndHashCode
@NoArgsConstructor
public class AppUser {
    @Id
    @SequenceGenerator(
        name = "user_sequence",
        sequenceName = "user_sequence",
        allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_sequence"
    )
    private Integer id;
    private String username;
    private String nickname;
    private String email;
    @JsonIgnore
    private String password;
    @Enumerated(EnumType.STRING)
    private AppUserRole role;
    @Convert(converter = LocalDateToStringConverter.class)
    private LocalDate creationDate;
    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Post> posts;
    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Comment> comments;

    public AppUser(String username, String nickname, String email, String password, AppUserRole role, LocalDate creationDate) {
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.role = role;
        this.creationDate = creationDate;
    }
}
