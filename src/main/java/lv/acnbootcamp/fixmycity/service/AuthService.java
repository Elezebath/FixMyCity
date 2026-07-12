package lv.acnbootcamp.fixmycity.service;

import lv.acnbootcamp.fixmycity.dto.RegisterRequest;
import lv.acnbootcamp.fixmycity.dto.UserResponse;
import lv.acnbootcamp.fixmycity.entity.User;
import lv.acnbootcamp.fixmycity.exception.EmailAlreadyExistsException;
import lv.acnbootcamp.fixmycity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .email(request.email())
                // Never store the raw password - always hash it first.
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(request.role())
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }
}