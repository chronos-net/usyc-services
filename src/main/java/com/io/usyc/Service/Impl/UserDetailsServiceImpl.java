package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.AppUser;
import com.io.usyc.Repository.AppUserRepository;
import com.io.usyc.Service.SecurityUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AppUserRepository userRepository;

    public UserDetailsServiceImpl(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        String normalized = (username == null) ? null : username.trim().toLowerCase();

        AppUser user = userRepository.findByUsernameWithRoles(normalized)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + normalized));

        List<GrantedAuthority> authorities = user.getUserRoles().stream()
                .map(ur -> (GrantedAuthority) SecurityUserDetails.role(ur.getRole().getCode()))
                .toList();

        return new SecurityUserDetails(user, authorities);
    }


}
