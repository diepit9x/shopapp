package com.project.shopapp.controllers;

import com.project.shopapp.dtos.UpdateUserDTO;
import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.dtos.UserLoginDTO;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.LoginResponse;
import com.project.shopapp.responses.RegisterResponse;
import com.project.shopapp.responses.UserResponse;
import com.project.shopapp.services.IUserService;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/users")
public class UserController {
    private final IUserService userService;
    private final LocalizationUtils localizationUtils;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result
    ) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessage =  result.getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(
                        RegisterResponse.builder()
                                .message(errorMessage.toString())
                                .build()
                );
            }

            if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
                return ResponseEntity.badRequest().body(
                        RegisterResponse.builder()
                                .message(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH))
                                .build()
                );
            }
            User user = userService.createUser(userDTO);
            return ResponseEntity.ok(
                    RegisterResponse.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_SUCCESSFULLY))
                            .user(user)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    RegisterResponse.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_FAILED, e.getMessage()))
                            .build()
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO,
            BindingResult result
    ) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessage =  result.getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(
                        LoginResponse.builder()
                                .message(errorMessage.toString())
                                .build());
            }
            String token = userService.login(userLoginDTO.getPhoneNumber(), userLoginDTO.getPassword(), userLoginDTO.getRoleId());
            return ResponseEntity.ok(
                    LoginResponse.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                            .token(token)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    LoginResponse.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED, e.getMessage()))
                            .build()
            );
        }

    }

    @PostMapping("/details")
    public ResponseEntity<UserResponse> getUserDetails(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String extractedToken = authorizationHeader.substring(7); // Loại bỏ "Bearer " từ chuỗi token
            User user = userService.getUserDetailsFromToken(extractedToken);
            return ResponseEntity.ok(UserResponse.fromUser(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @PutMapping("/details/{userId}")
    public ResponseEntity<?> updateUserDetails(
            @PathVariable Long userId,
            @RequestBody UpdateUserDTO updatedUserDTO,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String extractedToken = authorizationHeader.substring(7);
            User user = userService.getUserDetailsFromToken(extractedToken);
            // Ensure that the user making the request matches the user being updated
            if (user.getId() != userId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            User updatedUser = userService.updateUser(userId, updatedUserDTO);
            return ResponseEntity.ok(UserResponse.fromUser(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
