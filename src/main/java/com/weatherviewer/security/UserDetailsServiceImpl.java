package com.weatherviewer.security;

import com.weatherviewer.exception.notfound.UserNotFoundException;
import com.weatherviewer.model.User;
import com.weatherviewer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Loads a {@link SecUser} principal by email for Spring Security's
 * authentication process. Registered as the app's {@link UserDetailsService}
 * and consumed by the DAO authentication provider configured in
 * {@link com.weatherviewer.config.SecurityConfig}.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * @param username the account's email/login
     * @throws UserNotFoundException if no account matches the given email
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException("USER NOT FOUND"));
        return SecUser.fromUser(user);
    }

}
