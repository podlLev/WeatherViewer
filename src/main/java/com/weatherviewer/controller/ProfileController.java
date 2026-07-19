package com.weatherviewer.controller;

import com.weatherviewer.dto.UpdateUserDto;
import com.weatherviewer.model.User;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

/**
 * Thymeleaf controller for the current user's account profile page:
 * displaying stored profile fields and applying edits to them.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserService userService;

    /**
     * Spring Security 6 defaults to "explicit save" mode: {@code SecurityContextHolderFilter}
     * loads the session's context at the start of a request but no longer writes
     * {@link SecurityContextHolder}'s context back to the session automatically at
     * the end of one. Mutating {@link SecurityContextHolder} alone (as earlier
     * versions of this code did) therefore only lasted for the remainder of the
     * current request — the next request would reload the untouched, stale
     * session copy. This repository lets {@link #refreshAuthenticatedPrincipal}
     * write the refreshed context back into the session explicitly, the same
     * place {@link com.weatherviewer.security.UserDetailsServiceImpl}-backed
     * login already stores it, so it's actually still there on the next request.
     */
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

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
                                HttpServletRequest request, HttpServletResponse response,
                                RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            User userEntity = userService.getEntityById(user.getId());
            model.addAttribute("login", userEntity.getFullName());
            return "profile";
        }

        userService.update(user.getId(), dto);
        refreshAuthenticatedPrincipal(user.getId(), request, response);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/profile";
    }

    /**
     * Replaces the session's authenticated principal with a fresh {@link SecUser}
     * built from the just-saved entity.
     * <p>
     * {@code @AuthenticationPrincipal SecUser} is the same immutable object that
     * was built once at sign-in and stored in the session's {@code SecurityContext}
     * ({@link com.weatherviewer.security.UserDetailsServiceImpl}). Saving profile
     * changes to the database does not, by itself, update that cached principal -
     * every other controller reading {@code user.getUnits()} (or full name, email,
     * etc.) off {@code @AuthenticationPrincipal} would keep seeing the pre-update
     * values for the rest of the session, e.g. the dashboard silently continuing
     * to render in the old unit system after switching Celsius/Fahrenheit until
     * the user logs out and back in. Rebuilding and re-setting the principal here
     * makes the change visible immediately, on the very next request.
     */
    private void refreshAuthenticatedPrincipal(UUID userId, HttpServletRequest request, HttpServletResponse response) {
        User updatedEntity = userService.getEntityById(userId);
        SecUser refreshedPrincipal = SecUser.fromUser(updatedEntity);

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        Authentication refreshedAuth = new UsernamePasswordAuthenticationToken(
                refreshedPrincipal, currentAuth.getCredentials(), refreshedPrincipal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(refreshedAuth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

}
