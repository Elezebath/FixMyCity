package lv.acnbootcamp.fixmycity.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lv.acnbootcamp.fixmycity.entity.user.Role;
import lv.acnbootcamp.fixmycity.entity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetailsImpl userDetails;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "mySecretKeyThatIsLongEnoughForHS256Algorithm123456789",
                3600000
        );

        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setEnabled(true);
        user.setRole(Role.CITIZEN);

        userDetails = new UserDetailsImpl(user);
    }

    @Test
    void shouldGenerateToken() {

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractUsernameFromGeneratedToken() {

        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals(user.getEmail(), username);
    }

    @Test
    void shouldReturnFalseWhenUserIsDisabled() {

        String token = jwtService.generateToken(userDetails);

        user.setEnabled(false);

        boolean result = jwtService.isTokenValid(token, userDetails);

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueForValidToken() {

        String token = jwtService.generateToken(userDetails);

        boolean result = jwtService.isTokenValid(token, userDetails);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenUsernameDoesNotMatch() {

        String token = jwtService.generateToken(userDetails);

        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password");
        anotherUser.setFullName("Another User");
        anotherUser.setRole(Role.CITIZEN);
        anotherUser.setEnabled(true);

        UserDetailsImpl anotherUserDetails = new UserDetailsImpl(anotherUser);

        boolean result = jwtService.isTokenValid(token, anotherUserDetails);

        assertFalse(result);
    }

    @Test
    void shouldThrowExceptionWhenTokenIsExpired() throws InterruptedException {

        JwtService shortLivedJwtService = new JwtService(
                "mySecretKeyThatIsLongEnoughForHS256Algorithm123456789",
                1
        );

        String token = shortLivedJwtService.generateToken(userDetails);

        Thread.sleep(10);

        assertThrows(
                ExpiredJwtException.class,
                () -> shortLivedJwtService.isTokenValid(token, userDetails)
        );
    }

    @Test
    void shouldThrowExceptionForMalformedToken() {

        assertThrows(
                JwtException.class,
                () -> jwtService.extractUsername("abc123")
        );
    }

    @Test
    void shouldThrowExceptionForEmptyToken() {

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.extractUsername("")
        );
    }

    @Test
    void shouldThrowExceptionForNullToken() {

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.extractUsername(null)
        );
    }

    @Test
    void shouldThrowExceptionWhenTokenIsSignedWithDifferentSecret() {

        JwtService firstService = new JwtService(
                "mySecretKeyThatIsLongEnoughForHS256Algorithm123456789",
                3600000
        );

        JwtService secondService = new JwtService(
                "anotherSecretKeyThatIsLongEnoughForHS256Algorithm987654321",
                3600000
        );

        String token = firstService.generateToken(userDetails);

        assertThrows(
                JwtException.class,
                () -> secondService.extractUsername(token)
        );
    }


}