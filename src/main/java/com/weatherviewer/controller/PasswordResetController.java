package com.weatherviewer.controller;

import com.weatherviewer.dto.ForgotPasswordDto;
import com.weatherviewer.dto.ResetPasswordDto;
import com.weatherviewer.exception.InvalidTokenException;
import com.weatherviewer.service.VerificationService;
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
 * Thymeleaf controller for the "forgot your password" flow: requesting a
 * reset link by email, then redeeming that link to set a new password.
 * <p>
 * The request step always shows the same confirmation message whether or
 * not the email is registered, so this flow can't be used to enumerate
 * which addresses have accounts.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private static final String GENERIC_REQUEST_MESSAGE =
            "If an account exists for that email, we've sent a link to reset your password.";

    private final VerificationService verificationService;

    /** Renders the "forgot password" email-entry form. */
    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute("forgotPasswordDto", new ForgotPasswordDto());
        return "forgot-password";
    }

    /** Issues a reset token for the given email (if it exists) and always shows the same confirmation. */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@Valid @ModelAttribute("forgotPasswordDto") ForgotPasswordDto dto,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "forgot-password";
        }

        log.info("Password reset requested for email={}", dto.getEmail());
        verificationService.requestPasswordReset(dto.getEmail());

        redirectAttributes.addFlashAttribute("successMessage", GENERIC_REQUEST_MESSAGE);
        return "redirect:/sign-in";
    }

    /** Renders the "choose a new password" form for a token carried in the link. */
    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam String token, Model model) {
        model.addAttribute("resetPasswordDto", new ResetPasswordDto().setToken(token));
        return "reset-password";
    }

    /** Redeems the token and sets the new password, or re-renders the form with an error if the token/password is invalid. */
    @PostMapping("/reset-password")
    public String processResetPassword(@Valid @ModelAttribute("resetPasswordDto") ResetPasswordDto dto,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        if (!bindingResult.hasErrors() && !dto.getPassword().equals(dto.getRepeatPassword())) {
            bindingResult.rejectValue("repeatPassword", "password.mismatch", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            return "reset-password";
        }

        try {
            verificationService.resetPassword(dto.getToken(), dto.getPassword());
        } catch (InvalidTokenException e) {
            log.warn("Password reset failed: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage() + " Please request a new reset link.");
            return "reset-password";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Your password has been reset. You can now sign in.");
        return "redirect:/sign-in";
    }

}
