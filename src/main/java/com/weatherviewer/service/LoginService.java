package com.weatherviewer.service;

import jakarta.servlet.ServletException;

public interface LoginService {

    void login(String username, String password) throws ServletException;

}
