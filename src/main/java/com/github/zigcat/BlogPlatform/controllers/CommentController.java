package com.github.zigcat.BlogPlatform.controllers;

import com.github.zigcat.BlogPlatform.models.AppUserRole;
import com.github.zigcat.BlogPlatform.models.Comment;
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
@RequestMapping("api/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentRepository repository;
    private final PostRepository postRepository;
    private final AppUserRepository userRepository;

    record NewCommentRequest(
            String content,
            Integer post
    ){}

    record UpdateCommentRequest(
            String content
    ){}

    @GetMapping
    public List<Comment> getComments(){return repository.findAll();}

    @GetMapping("/post/{post_id}")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable("post_id") Integer id){
        try{
            Post post = postRepository.findById(id).get();
            return new ResponseEntity<>(post.getComments(), HttpStatus.OK);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/id/{comment_id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable("comment_id") Integer id){
        try{
            Comment comment = repository.findById(id).get();
            return new ResponseEntity<>(comment, HttpStatus.OK);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Comment> createComment(@RequestBody NewCommentRequest request,
                                                 @AuthenticationPrincipal UserDetails userAuth){
        if(userAuth == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Comment comment = new Comment();
        comment.setContent(request.content());
        comment.setPost(postRepository.findById(request.post()).get());
        comment.setUser(userRepository.findByUsername(userAuth.getUsername()));
        comment.setCreationDate(LocalDate.now());
        repository.save(comment);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/update/{comment_id}")
    public ResponseEntity<Comment> updateComment(@PathVariable("comment_id") Integer id,
                                                 @RequestBody UpdateCommentRequest request,
                                                 @AuthenticationPrincipal UserDetails userAuth){
        try{
            Comment comment = repository.findById(id).get();
            if(comment.getUser().getUsername().equals(userAuth.getUsername())){
                comment.setContent(request.content());
                repository.save(comment);
                return new ResponseEntity<>(comment, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete/{comment_id}")
    public ResponseEntity<Comment> deleteComment(@PathVariable("comment_id") Integer id,
                                                 @AuthenticationPrincipal UserDetails userAuth){
        try{
            Comment comment = repository.findById(id).get();
            if(comment.getUser().getUsername().equals(userAuth.getUsername()) ||
                    userAuth.getAuthorities().contains(new SimpleGrantedAuthority(AppUserRole.ADMIN.toString()))){
                repository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch(NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
