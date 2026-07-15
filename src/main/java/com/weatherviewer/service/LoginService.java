package com.weatherviewer.service;

import jakarta.servlet.ServletException;

/**
 * Programmatic sign-in used to authenticate a user outside of the normal
 * Spring Security form-login filter chain (e.g. auto-login immediately
 * after successful self-registration).
 */
public interface LoginService {

    /**
     * Authenticates the given credentials and establishes a security
     * context/session for the current request, as if the user had signed
     * in through the login form.
     *
     * @param username the account's email/login
     * @param password the account's plain-text password
     * @throws ServletException if the underlying authentication mechanism fails
     */
    void login(String username, String password) throws ServletException;

}
