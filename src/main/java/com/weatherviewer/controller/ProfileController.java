package com.weatherviewer.controller;

import com.weatherviewer.dto.UpdateUserDto;
import com.weatherviewer.model.User;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Thymeleaf controller for the current user's account profile page:
 * displaying stored profile fields and applying edits to them.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserService userService;

    /** Renders the profile page, pre-populated with the signed-in user's current details. */
    @GetMapping("/profile")
    public String getProfile(@AuthenticationPrincipal SecUser user, Model model) {
        log.info("Displaying profile page for user '{}'", user.getUsername());

        User userEntity = userService.getEntityById(user.getId());

        model.addAttribute("login", user.getFullName());
        model.addAttribute("user", userEntity);
        return "profile";
    }

    /**
     * Handles profile update submissions. On validation failure, re-renders
     * the profile page with the errors; on success, applies the update,
     * flashes a confirmation message, and redirects back to {@code /profile}.
     */
    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("user") UpdateUserDto dto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal SecUser user,
                                RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            User userEntity = userService.getEntityById(user.getId());
            model.addAttribute("login", userEntity.getFullName());
            return "profile";
        }

        userService.update(user.getId(), dto);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/profile";
    }

}
