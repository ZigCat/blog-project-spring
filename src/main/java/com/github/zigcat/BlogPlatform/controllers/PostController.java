package com.github.zigcat.BlogPlatform.controllers;

import com.github.zigcat.BlogPlatform.models.AppUser;
import com.github.zigcat.BlogPlatform.models.AppUserRole;
import com.github.zigcat.BlogPlatform.models.Post;
import com.github.zigcat.BlogPlatform.repositories.AppUserRepository;
import com.github.zigcat.BlogPlatform.repositories.CommentRepository;
import com.github.zigcat.BlogPlatform.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("api/post")
@RequiredArgsConstructor
public class PostController {
    private final PostRepository repository;
    private final AppUserRepository userRepository;
    private final CommentRepository commentRepository;

    record NewPostRequest(
            String content
    ){}

    @GetMapping
    public List<Post> getPosts(){
        return repository.findAll();
    }

    @GetMapping("/id/{post_id}")
    public ResponseEntity<Post> getPostById(@PathVariable("post_id") Integer id){
        try{
            Post post = repository.findById(id).get();
            return new ResponseEntity<>(post, HttpStatus.OK);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{user_id}")
    public ResponseEntity<List<Post>> getPostsByUser(@PathVariable("user_id") Integer id){
        try{
            AppUser user = userRepository.findById(id).get();
            return new ResponseEntity<>(user.getPosts(), HttpStatus.OK);
        } catch(NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Post> addPost(@RequestBody NewPostRequest request,
                                        @AuthenticationPrincipal UserDetails userAuth){
        if(userAuth == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else {
            Post post = new Post();
            post.setContent(request.content());
            post.setUser(userRepository.findByUsername(userAuth.getUsername()));
            post.setCreationDate(LocalDate.now());
            repository.save(post);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @PatchMapping("/update/{post_id}")
    public ResponseEntity<Post> updatePost(@PathVariable("post_id") Integer id,
                                           @RequestBody NewPostRequest request,
                                           @AuthenticationPrincipal UserDetails userAuth){
        try{
            Post post = repository.findById(id).get();
            if(post.getUser().getUsername().equals(userAuth.getUsername())){
                post.setContent(request.content());
                repository.save(post);
                return new ResponseEntity<>(post, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete/{post_id}")
    public ResponseEntity<Post> deletePost(@PathVariable("post_id") Integer id,
                                           @AuthenticationPrincipal UserDetails userAuth){
        try{
            Post post = repository.findById(id).get();
            if(post.getUser().getUsername().equals(userAuth.getUsername()) ||
                    userAuth.getAuthorities().contains(new SimpleGrantedAuthority(AppUserRole.ADMIN.toString()))){
                commentRepository.deleteAllInBatch(post.getComments());
                repository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
