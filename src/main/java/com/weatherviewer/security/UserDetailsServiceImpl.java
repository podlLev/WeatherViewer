package com.weatherviewer.security;

import com.weatherviewer.exception.notfound.UserNotFoundException;
import com.weatherviewer.model.User;
import com.weatherviewer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException("USER NOT FOUND"));
        return SecUser.fromUser(user);
    }

}
