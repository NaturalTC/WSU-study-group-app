package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// TODO: Jose — required by Spring Security
// Spring calls loadUserByUsername() to look up a user before validating their password or token
// "username" in Spring Security = email in our app

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // TODO: find the user by email using userRepository.findByEmail(email)
        // TODO: if not found, throw new UsernameNotFoundException("User not found: " + email)
        // TODO: return org.springframework.security.core.userdetails.User
        //  .withUsername(user.getEmail())
        //  .password(user.getPassword())
        //  .roles("USER")
        //  .build()
        return null;
    }
}
