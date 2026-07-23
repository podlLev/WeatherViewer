package com.weatherviewer.controller;

import com.weatherviewer.dto.CreateUserDto;
import com.weatherviewer.exception.InvalidTokenException;
import com.weatherviewer.model.User;
import com.weatherviewer.service.LoginService;
import com.weatherviewer.service.UserService;
import com.weatherviewer.service.VerificationService;
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

import java.util.UUID;

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
    private final VerificationService verificationService;

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
    public String signInFailure(@RequestParam(required = false) Boolean unverified,
                                @RequestParam(required = false) Boolean locked,
                                @RequestParam(required = false) String email,
                                RedirectAttributes redirectAttributes) {
        log.info("Sign-in failed, unverified={}, locked={}", unverified, locked);

        String emailParam = email != null && !email.isBlank() ? "&email=" + email : "";
        if (Boolean.TRUE.equals(unverified)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Please verify your email before signing in.");
            return "redirect:/sign-in?unverified=true" + emailParam;
        }

        if (Boolean.TRUE.equals(locked)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Too many failed sign-in attempts. Your account is temporarily locked — please try again in a few minutes.");
            return "redirect:/sign-in?locked=true" + emailParam;
        }

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
        UUID userId = userService.create(createUserDto);

        User createdUser = userService.getEntityById(userId);
        verificationService.sendVerificationEmail(createdUser);

        try {
            loginService.login(createUserDto.getEmail(), createUserDto.getPassword());
            log.info("Auto login successful for email={}", createUserDto.getEmail());
        } catch (ServletException e) {
            log.info("Auto login skipped/failed after sign-up for email={} (account likely pending email verification)",
                    createUserDto.getEmail());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Account created! Check your email for a link to verify your account before signing in.");
            return "redirect:/sign-in";
        }

        log.info("Account created successfully for email={}", createUserDto.getEmail());
        redirectAttributes.addFlashAttribute("successMessage", "Account created successfully");
        return "redirect:" + SafeRedirectUtils.sanitize(redirect, "/");
    }

    /** Redeems an emailed email-verification link, activating the account, then sends the user to sign in. */
    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token, RedirectAttributes redirectAttributes) {
        try {
            verificationService.confirmEmail(token);
            log.info("Email verified successfully for token");
            redirectAttributes.addFlashAttribute("successMessage", "Your email has been verified. You can now sign in.");
        } catch (InvalidTokenException e) {
            log.warn("Email verification failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    e.getMessage() + " Please request a new verification email.");
        }
        return "redirect:/sign-in";
    }

    /**
     * Re-sends the verification email for an account that hasn't confirmed
     * its address yet. Always shows the same confirmation message
     * regardless of whether the email exists or is already verified, so
     * this can't be used to enumerate registered addresses.
     */
    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam String email, RedirectAttributes redirectAttributes) {
        log.info("Resend verification requested for email={}", email);
        try {
            User user = userService.getEntityByEmail(email);
            if (user.getStatus() == com.weatherviewer.model.enums.UserStatus.PENDING) {
                verificationService.sendVerificationEmail(user);
            }
        } catch (RuntimeException e) {
            log.info("Resend verification requested for unknown email={}", email);
        }

        redirectAttributes.addFlashAttribute("successMessage",
                "If that account needs verifying, we've sent a fresh link to its email address.");
        return "redirect:/sign-in";
    }

}
