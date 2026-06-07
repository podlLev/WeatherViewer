package com.weatherviewer.service.impl;

import com.weatherviewer.service.LoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final HttpServletRequest request;

    @Override
    public void login(String username, String password) throws ServletException {
        request.login(username, password);
    }

}
