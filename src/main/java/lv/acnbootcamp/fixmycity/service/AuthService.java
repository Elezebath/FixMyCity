package lv.acnbootcamp.fixmycity.service;

import lv.acnbootcamp.fixmycity.dto.auth.RegisterRequest;
import lv.acnbootcamp.fixmycity.dto.user.UserResponse;
import lv.acnbootcamp.fixmycity.entity.User;
import lv.acnbootcamp.fixmycity.exception.user.EmailAlreadyExistsException;
import lv.acnbootcamp.fixmycity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lv.acnbootcamp.fixmycity.entity.Role;
import lv.acnbootcamp.fixmycity.dto.auth.LoginRequest;
import lv.acnbootcamp.fixmycity.dto.auth.LoginResponse;
import lv.acnbootcamp.fixmycity.security.JwtService;
import lv.acnbootcamp.fixmycity.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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
                .role(Role.CITIZEN)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    /**
     * Authenticates the user's credentials and returns a JWT access token.
     */
    public LoginResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        UserDetailsImpl userDetails =
                (UserDetailsImpl) authentication.getPrincipal();

        User user = userDetails.getUser();

        String accessToken = jwtService.generateToken(userDetails);

        return new LoginResponse(
                accessToken,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole());
    }
}