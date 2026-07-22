package lv.acnbootcamp.fixmycity.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lv.acnbootcamp.fixmycity.dto.user.CreateUserByAdminRequest;
import lv.acnbootcamp.fixmycity.dto.user.UpdateUserProfileRequest;
import lv.acnbootcamp.fixmycity.dto.user.UpdateUserRoleRequest;
import lv.acnbootcamp.fixmycity.dto.user.UpdateUserStatusRequest;
import lv.acnbootcamp.fixmycity.dto.user.UserAdminResponse;
import lv.acnbootcamp.fixmycity.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "User Administration", description = "Admin-only endpoints for managing user accounts (create, edit, activate/deactivate, change role)")
@SecurityRequirement(name = "bearerAuth")
public class UserAdminController {

    private final UserService userService;

    public UserAdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(
            summary = "List all users",
            description = "Returns all registered users. Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have ADMIN role")
    })
    public ResponseEntity<List<UserAdminResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a user by ID",
            description = "Returns a single user's details. Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "No user exists with the given ID")
    })
    public ResponseEntity<UserAdminResponse> getUserById(
            @Parameter(description = "ID of the user to retrieve") @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    @Operation(
            summary = "Create a new user",
            description = "Creates a user with an arbitrary role, bypassing the public registration flow (which always assigns CITIZEN). Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed (e.g. invalid email, password too short, missing role)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have ADMIN role"),
            @ApiResponse(responseCode = "409", description = "A user with this email already exists")
    })
    public ResponseEntity<UserAdminResponse> createUser(
            @Valid @RequestBody CreateUserByAdminRequest request) {
        UserAdminResponse created = userService.createUser(
                request.getEmail(), request.getPassword(), request.getFullName(), request.getRole(), request.getCompany());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/profile")
    @Operation(
            summary = "Update a user's profile",
            description = "Updates a user's email and full name. Does not change password or role. Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed (e.g. invalid email, blank full name)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "No user exists with the given ID"),
            @ApiResponse(responseCode = "409", description = "Another user already uses the given email")
    })
    public ResponseEntity<UserAdminResponse> updateProfile(
            @Parameter(description = "ID of the user to update") @PathVariable Long id,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        return ResponseEntity.ok(
                userService.updateUserProfile(id, request.getEmail(), request.getFullName()));
    }

    @PatchMapping("/{id}/role")
    @Operation(
            summary = "Change a user's role",
            description = "Updates the role assigned to a user (CITIZEN, MANAGER, COMPANY, ADMIN). Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed (e.g. missing role)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "No user exists with the given ID")
    })
    public ResponseEntity<UserAdminResponse> updateRole(
            @Parameter(description = "ID of the user to update") @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(userService.updateUserRole(id, request.getRole()));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Enable or disable a user",
            description = "Activates or deactivates a user account. Disabled users cannot authenticate. Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed (e.g. missing enabled flag)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "No user exists with the given ID")
    })
    public ResponseEntity<UserAdminResponse> updateStatus(
            @Parameter(description = "ID of the user to update") @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(userService.updateUserStatus(id, request.getEnabled()));
    }
}