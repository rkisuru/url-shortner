package com.rkisuru.url_shortner.user_sevice.controller;

import com.rkisuru.url_shortner.user_sevice.dto.UserResponse;
import com.rkisuru.url_shortner.user_sevice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * User profile endpoints — requires authentication.
 *
 * The userId is extracted from the JWT by the JwtAuthenticationFilter
 * and stored as a request attribute.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        return ResponseEntity.ok(userService.getUserById(UUID.fromString(userId)));
    }
}
