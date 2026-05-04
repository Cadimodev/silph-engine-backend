package com.silphengine.infrastructure.web.exceptions;

import com.silphengine.domain.exceptions.BadRequestException;
import com.silphengine.domain.exceptions.DuplicateResourceException;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.exceptions.TokenRefreshException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldHandleResourceNotFoundException_Returns404() throws Exception {
        mockMvc.perform(get("/test/not-found")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    void shouldHandleDuplicateResourceException_Returns409() throws Exception {
        mockMvc.perform(get("/test/duplicate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Resource already exists"));
    }

    @Test
    void shouldHandleBadRequestException_Returns400() throws Exception {
        mockMvc.perform(get("/test/bad-request")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid data"));
    }

    @Test
    void shouldHandleTokenRefreshException_Returns403() throws Exception {
        mockMvc.perform(get("/test/token-refresh")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Failed for token [dummy-token-123]: Refresh token expired"));
    }

    @Test
    void shouldHandleAuthenticationException_Returns401() throws Exception {
        mockMvc.perform(get("/test/auth-error")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials provided."));
    }

    @Test
    void shouldHandleMethodArgumentNotValidException_Returns400WithDetails() throws Exception {
        String invalidPayload = "{\"name\": \"\"}";

        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.name").value("Name is mandatory"));
    }

    @Test
    void shouldHandleGenericException_Returns500() throws Exception {
        mockMvc.perform(get("/test/generic-error")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."));
    }

    record DummyRequest(@NotBlank(message = "Name is mandatory") String name) {}

    @RestController
    static class DummyController {

        @GetMapping("/test/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Resource not found");
        }

        @GetMapping("/test/duplicate")
        public void throwDuplicate() {
            throw new DuplicateResourceException("Resource already exists");
        }

        @GetMapping("/test/bad-request")
        public void throwBadRequest() {
            throw new BadRequestException("Invalid data");
        }

        @GetMapping("/test/token-refresh")
        public void throwTokenRefresh() {
            throw new TokenRefreshException("dummy-token-123", "Refresh token expired");
        }

        @GetMapping("/test/auth-error")
        public void throwAuthError() {
            throw new BadCredentialsException("Bad credentials");
        }

        @PostMapping("/test/validation")
        public void throwValidationError(@Valid @RequestBody DummyRequest request) {
            // MethodArgumentNotValidException is automatically thrown if it is not valid.
        }

        @GetMapping("/test/generic-error")
        public void throwGenericError() throws Exception {
            throw new Exception("Some unhandled error");
        }
    }
}
