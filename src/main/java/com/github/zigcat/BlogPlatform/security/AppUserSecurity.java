package com.github.zigcat.BlogPlatform.security;

import com.github.zigcat.BlogPlatform.models.AppUser;
import com.github.zigcat.BlogPlatform.models.AppUserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;

public class AppUserSecurity implements UserDetails {
    private AppUser user;

    public AppUserSecurity(AppUser user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(user.getRole().equals(AppUserRole.ADMIN)){
            return Arrays.asList(new SimpleGrantedAuthority(user.getRole().toString()), new SimpleGrantedAuthority(AppUserRole.USER.toString()));
        } else {
            return Arrays.asList(new SimpleGrantedAuthority(user.getRole().toString()));
        }
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
