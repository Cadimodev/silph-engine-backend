package com.silphengine.domain.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotBlank(message = "Old password is mandatory")
        String oldPassword,

        @NotBlank(message = "New password is mandatory")
        @Size(min = 8, message = "New password must be at least 8 characters long")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!¡?¿])(?=\\S+$).{8,}$",
                message = "Password must contain at least one digit, one lowercase, one uppercase, one special character, and no whitespaces"
        )
        String newPassword
) {}
