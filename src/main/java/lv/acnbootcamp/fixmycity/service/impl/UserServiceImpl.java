package lv.acnbootcamp.fixmycity.service.impl;

import lv.acnbootcamp.fixmycity.dto.user.UserAdminResponse;
import lv.acnbootcamp.fixmycity.entity.AuditAction;
import lv.acnbootcamp.fixmycity.entity.AuditEntityType;
import lv.acnbootcamp.fixmycity.entity.Role;
import lv.acnbootcamp.fixmycity.entity.User;
import lv.acnbootcamp.fixmycity.exception.user.EmailAlreadyExistsException;
import lv.acnbootcamp.fixmycity.exception.user.UserNotFoundException;
import lv.acnbootcamp.fixmycity.repository.UserRepository;
import lv.acnbootcamp.fixmycity.service.AuditLogService;
import lv.acnbootcamp.fixmycity.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<UserAdminResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public UserAdminResponse getUserById(Long id) {
        return mapToResponse(findUserOrThrow(id));
    }

    @Override
    public UserAdminResponse createUser(String email, String rawPassword, String fullName, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .role(role)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        auditLogService.log(AuditEntityType.USER, saved.getId(), AuditAction.CREATE,
                "Created user '" + saved.getEmail() + "' with role " + saved.getRole());
        return mapToResponse(saved);
    }

    @Override
    public UserAdminResponse updateUserProfile(Long id, String email, String fullName) {
        User user = findUserOrThrow(id);

        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        String oldEmail = user.getEmail();
        user.setEmail(email);
        user.setFullName(fullName);

        User saved = userRepository.save(user);
        auditLogService.log(AuditEntityType.USER, saved.getId(), AuditAction.UPDATE,
                "Updated profile: email '" + oldEmail + "' -> '" + saved.getEmail() + "'");
        return mapToResponse(saved);
    }

    @Override
    public UserAdminResponse updateUserRole(Long id, Role newRole) {
        User user = findUserOrThrow(id);
        Role oldRole = user.getRole();
        user.setRole(newRole);

        User saved = userRepository.save(user);
        auditLogService.log(AuditEntityType.USER, saved.getId(), AuditAction.UPDATE,
                "Changed role: " + oldRole + " -> " + newRole);
        return mapToResponse(saved);
    }

    @Override
    public UserAdminResponse updateUserStatus(Long id, boolean enabled) {
        User user = findUserOrThrow(id);
        user.setEnabled(enabled);

        User saved = userRepository.save(user);
        auditLogService.log(AuditEntityType.USER, saved.getId(), AuditAction.UPDATE,
                (enabled ? "Enabled" : "Disabled") + " user '" + saved.getEmail() + "'");
        return mapToResponse(saved);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private UserAdminResponse mapToResponse(User user) {
        return UserAdminResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}