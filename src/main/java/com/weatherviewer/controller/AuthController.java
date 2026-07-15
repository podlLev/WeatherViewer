package com.weatherviewer.controller;

import com.weatherviewer.dto.CreateUserDto;
import com.weatherviewer.service.LoginService;
import com.weatherviewer.service.UserService;
import com.weatherviewer.utils.SafeRedirectUtils;
import jakarta.servlet.ServletException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Thymeleaf controller for the sign-in/sign-up pages and their form
 * submissions. Successful sign-up auto-logs the new user in via
 * {@link LoginService} so they land directly on the app instead of having
 * to sign in a second time.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final LoginService loginService;

    /** Renders the sign-in form, preserving a sanitized post-login redirect target if one was supplied. */
    @GetMapping("/sign-in")
    public String signIn(@RequestParam(required = false) String redirect, Model model) {
        String safeRedirect = SafeRedirectUtils.sanitize(redirect, null);
        log.info("Displaying sign-in page, redirect={}", safeRedirect);
        model.addAttribute("redirect", safeRedirect);
        return "sign-in";
    }

    /** Landing target Spring Security redirects to after a failed login attempt; re-renders sign-in with an error. */
    @GetMapping("/sign-in-failure")
    public String signInFailure(RedirectAttributes redirectAttributes) {
        log.info("Sign-in failed");
        redirectAttributes.addFlashAttribute("errorMessage", "Invalid email or password. Please try again.");
        return "redirect:/sign-in";
    }

    /** Renders the registration form. */
    @GetMapping("/sign-up")
    public String signUp(@RequestParam(required = false) String redirect, Model model) {
        String safeRedirect = SafeRedirectUtils.sanitize(redirect, null);
        log.info("Displaying sign-up page, redirect={}", safeRedirect);
        model.addAttribute("user", new CreateUserDto());
        model.addAttribute("redirect", safeRedirect);
        return "sign-up";
    }

    /**
     * Handles registration submission: validates the payload, creates the
     * account, then attempts to auto-login the new user. If auto-login
     * fails, the account still exists — the user is sent to sign in
     * manually instead. On full success, redirects to the sanitized
     * {@code redirect} target or {@code /} by default.
     */
    @PostMapping("/sign-up")
    public String processSignUp(@Valid @ModelAttribute("user") CreateUserDto createUserDto,
                                BindingResult bindingResult,
                                @RequestParam(required = false) String redirect,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            log.info("Sign-up failed due to validation errors for email={}", createUserDto.getEmail());
            return "sign-up";
        }

        log.info("Processing sign-up for email={}", createUserDto.getEmail());
        userService.create(createUserDto);

        try {
            loginService.login(createUserDto.getEmail(), createUserDto.getPassword());
            log.info("Auto login successful for email={}", createUserDto.getEmail());
        } catch (ServletException e) {
            log.warn("Auto login failed after sign-up for email={}", createUserDto.getEmail(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Auto login failed after sign-up");
            return "redirect:/sign-in";
        }

        log.info("Account created successfully for email={}", createUserDto.getEmail());
        redirectAttributes.addFlashAttribute("successMessage", "Account created successfully");
        return "redirect:" + SafeRedirectUtils.sanitize(redirect, "/");
    }

}
