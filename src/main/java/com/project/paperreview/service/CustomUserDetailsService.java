package com.project.paperreview.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.project.paperreview.entity.User;
import com.project.paperreview.repository.UserRepository;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        System.out.println("LOGIN ATTEMPT: " + username);

        // 🔥 SAFE FETCH (handles case mismatch manually)
        User user = repo.findAll()
                        .stream()
                        .filter(u -> u.getUsername().equalsIgnoreCase(username))
                        .findFirst()
                        .orElse(null);

        if (user == null) {
            System.out.println("USER NOT FOUND ❌");
            throw new UsernameNotFoundException("User not found");
        }

        System.out.println("USER FOUND ✅");
        System.out.println("USERNAME: " + user.getUsername());
        System.out.println("PASSWORD: " + user.getPassword());
        System.out.println("ROLE: " + user.getRole());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}