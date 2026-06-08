package com.weatherviewer.controller;

import com.weatherviewer.dto.CreateUserDto;
import com.weatherviewer.service.LoginService;
import com.weatherviewer.service.UserService;
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

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final LoginService loginService;

    @GetMapping("/sign-in")
    public String signIn(@RequestParam(required = false) String redirect, Model model) {
        log.info("Displaying sign-in page, redirect={}", redirect);
        model.addAttribute("redirect", redirect);
        return "sign-in";
    }

    @GetMapping("/sign-in-failure")
    public String signInFailure(RedirectAttributes redirectAttributes) {
        log.info("Sign-in failed");
        redirectAttributes.addFlashAttribute("errorMessage", "Invalid email or password. Please try again.");
        return "redirect:/sign-in";
    }

    @GetMapping("/sign-up")
    public String signUp(@RequestParam(required = false) String redirect, Model model) {
        log.info("Displaying sign-up page, redirect={}", redirect);
        model.addAttribute("user", new CreateUserDto());
        model.addAttribute("redirect", redirect);
        return "sign-up";
    }

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
        return (redirect != null && !redirect.isBlank()) ? "redirect:" + redirect : "redirect:/";
    }

}
