package com.io.usyc.Service;

import com.io.usyc.Domain.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SecurityUserDetails implements UserDetails {

    private final AppUser user;
    private final List<GrantedAuthority> authorities;

    public SecurityUserDetails(AppUser user, List<GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    public AppUser getUser() { return user; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return user.getPasswordHash(); }
    @Override public String getUsername() { return user.getEmail(); }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.isActive(); }

    public static SimpleGrantedAuthority role(String code) {
        // Spring: ROLE_ADMIN, ROLE_CAJERO...
        return new SimpleGrantedAuthority("ROLE_" + code);
    }
}
