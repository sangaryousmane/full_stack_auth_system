package com.ous.aethererp.security;

import com.ous.aethererp.entity.UserEntity;
import com.ous.aethererp.repo.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AppUserDetailService implements UserDetailsService {

    private final UserEntityRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        UserEntity userByEmail = userRepo.findByEmail(userEmail)
                .orElseThrow(()-> new UsernameNotFoundException("Email not found for the email: "+ userEmail));
        List<SimpleGrantedAuthority> authorities= userByEmail.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();

        return new User(userByEmail.getEmail(), userByEmail.getPassword(), authorities);
    }
}
