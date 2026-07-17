package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Used by UserDetailsServiceImpl to look up a user during login.
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}