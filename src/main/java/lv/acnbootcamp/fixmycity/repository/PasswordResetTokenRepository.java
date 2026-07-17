package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.user.PasswordResetToken;
import lv.acnbootcamp.fixmycity.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    void deleteAllByUser(User user);
}