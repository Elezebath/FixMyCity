package lv.acnbootcamp.fixmycity.security;

import lv.acnbootcamp.fixmycity.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Wraps our own User entity so Spring Security can work with it.
// Spring Security doesn't know about our User class directly - it only
// understands the UserDetails contract, so this class bridges the two.
public class UserDetailsImpl implements UserDetails {

    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    // Exposes the role as a Spring Security "authority", prefixed with "ROLE_"
    // because that's the convention hasRole("ADMIN") expects internally
    // (it actually checks for "ROLE_ADMIN").
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // we authenticate by email, not a separate username field
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    public User getUser() {
        return user;
    }
}