package com.silphengine.infrastructure.web.controllers.view;

import com.silphengine.domain.dto.requests.UserRequest;
import com.silphengine.domain.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class RegisterViewController {

    private final AuthenticationService authenticationService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userRequest", new UserRequest("", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("userRequest") UserRequest userRequest,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            authenticationService.register(userRequest);
            return "redirect:/login?registered=true";

        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("businessError", e.getMessage());
            return "register";
        }
    }
}