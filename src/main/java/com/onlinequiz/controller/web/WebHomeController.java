package com.onlinequiz.controller.web;

import com.onlinequiz.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebHomeController {

    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal != null) {
            boolean isAdmin = userPrincipal.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            return isAdmin ? "redirect:/admin/dashboard" : "redirect:/dashboard";
        }
        return "redirect:/quizzes";
    }

    @GetMapping("/login")
    public String loginPage(@AuthenticationPrincipal UserPrincipal userPrincipal,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "registered", required = false) String registered,
                            Model model) {
        if (userPrincipal != null) {
            return "redirect:/";
        }
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password. Please try again.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Registration successful! You can now log in.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal != null) {
            return "redirect:/";
        }
        return "auth/register";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal != null) {
            return "redirect:/";
        }
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                    @RequestParam(value = "token", required = false) String token,
                                    Model model) {
        if (userPrincipal != null) {
            return "redirect:/";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }
}
