package com.github.zigcat.BlogPlatform.services;

import com.github.zigcat.BlogPlatform.models.AppUser;
import com.github.zigcat.BlogPlatform.repositories.AppUserRepository;
import com.github.zigcat.BlogPlatform.security.AppUserSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    @Autowired
    private AppUserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = repository.findByUsername(username);
        if(appUser == null){
            throw new UsernameNotFoundException("user not found");
        }
        return new AppUserSecurity(appUser);
    }
}
