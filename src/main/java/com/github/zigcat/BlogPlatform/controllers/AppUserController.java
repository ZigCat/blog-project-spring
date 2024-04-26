package com.github.zigcat.BlogPlatform.controllers;

import com.github.zigcat.BlogPlatform.models.AppUser;
import com.github.zigcat.BlogPlatform.models.AppUserRole;
import com.github.zigcat.BlogPlatform.repositories.AppUserRepository;
import com.github.zigcat.BlogPlatform.repositories.CommentRepository;
import com.github.zigcat.BlogPlatform.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
public class AppUserController {
    private final AppUserRepository repository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private static final Logger logger = LoggerFactory.getLogger(AppUserController.class);

    record NewAppUserRequest(
            String username,
            String nickname,
            String email,
            String password,
            String role
    ){}

    record PwdAppUserRequest(
            String password
    ){}

    @GetMapping()
    public List<AppUser> getUsers(){
        return repository.findAll();
    }

    @GetMapping("/id/{user_id}")
    public ResponseEntity<AppUser> getUserById(@PathVariable("user_id") Integer id){
        try{
            AppUser user = repository.findById(id).get();
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<AppUser>> searchByNickname(@RequestParam String search){
        List<AppUser> users = repository.findAll();
        List<AppUser> sortedResults = users.stream()
                .filter(item -> item.getNickname().toLowerCase().contains(search.toLowerCase()))
                .sorted(Comparator.comparing(item -> item.getNickname().equalsIgnoreCase(search) ? 0 : 1))
                .toList();
        return new ResponseEntity(sortedResults, HttpStatus.OK);
    }

    @GetMapping("/login")
    public ResponseEntity<AppUser> getAuth(@AuthenticationPrincipal UserDetails userAuth){
        try{
            AppUser user = repository.findByUsername(userAuth.getUsername());
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (NullPointerException e){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AppUser> addUser(@RequestBody NewAppUserRequest request,
                                           @AuthenticationPrincipal UserDetails userAuth){
        try{
            if(AppUserRole.valueOf(request.role()).equals(AppUserRole.ADMIN)){
                if(userAuth.getAuthorities().contains(new SimpleGrantedAuthority(AppUserRole.ADMIN.toString()))){
                    AppUser user = new AppUser();
                    user.setUsername(request.username());
                    user.setNickname(request.nickname());
                    user.setEmail(request.email());
                    user.setPassword(encoder.encode(request.password()));
                    user.setRole(AppUserRole.valueOf(request.role));
                    user.setCreationDate(LocalDate.now());
                    repository.save(user);
                    AppUser user1 = repository.findByUsername(user.getUsername());
                    return new ResponseEntity<>(user1, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            } else {
                AppUser user = new AppUser();
                user.setUsername(request.username());
                user.setNickname(request.nickname());
                user.setEmail(request.email());
                user.setPassword(encoder.encode(request.password()));
                user.setRole(AppUserRole.valueOf(request.role));
                user.setCreationDate(LocalDate.now());
                repository.save(user);
                AppUser user1 = repository.findByUsername(user.getUsername());
                return new ResponseEntity<>(user1, HttpStatus.OK);
            }
        } catch (NullPointerException e){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping("/delete/{user_id}")
    public ResponseEntity<AppUser> deleteUser(@PathVariable("user_id") Integer id,
                                              @AuthenticationPrincipal UserDetails authUser){
        try{
            AppUser user = repository.findById(id).get();
            if(authUser.getUsername().equals(user.getUsername()) ||
                    authUser.getAuthorities().contains(new SimpleGrantedAuthority(AppUserRole.ADMIN.toString()))){
                postRepository.deleteAllInBatch(user.getPosts());
                commentRepository.deleteAllInBatch(user.getComments());
                repository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/update/{user_id}")
    public ResponseEntity<AppUser> updateUser(@PathVariable("user_id") Integer id,
                           @RequestBody NewAppUserRequest request,
                            @AuthenticationPrincipal UserDetails authUser){
        try{
            AppUser user = repository.findById(id).get();
            if(authUser.getUsername().equals(user.getUsername())){
                user.setUsername(request.username());
                user.setNickname(request.nickname());
                user.setEmail(request.email());
                user.setRole(AppUserRole.valueOf(request.role));
                repository.save(user);
                return new ResponseEntity<>(repository.findById(id).get(), HttpStatus.OK);
            } else if(authUser.getAuthorities().contains(new SimpleGrantedAuthority(AppUserRole.ADMIN.toString()))){
                user.setRole(AppUserRole.valueOf(request.role));
                repository.save(user);
                return new ResponseEntity<>(repository.findById(id).get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/update/pwd/{user_id}")
    public ResponseEntity<AppUser> updatePwd(@PathVariable("user_id") Integer id,
                                             @RequestBody PwdAppUserRequest request,
                                             @AuthenticationPrincipal UserDetails authUser){
        try{
            AppUser user = repository.findById(id).get();
            if(user.getUsername().equals(authUser.getUsername())){
                user.setPassword(encoder.encode(request.password()));
                repository.save(user);
                return new ResponseEntity<>(user, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
