package com.auth.Service;
import com.auth.Entity.User;
import com.auth.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;

    public void loginFailed(User user) {
        user.setFailedAttempts(user.getFailedAttempts() + 1);

        if (user.getFailedAttempts() >= MAX_ATTEMPTS) {
            user.setAccountNonLocked(false);
            user.setLockTime(LocalDateTime.now());
        }

        userRepository.save(user);
    }

    public void loginSucceeded(User user) {
        user.setFailedAttempts(0);
        user.setAccountNonLocked(true);
        user.setLockTime(null);
        userRepository.save(user);
    }

    public boolean unlockIfExpired(User user) {
        if (!user.isAccountNonLocked()) {
            if (user.getLockTime()
                    .plusMinutes(LOCK_DURATION_MINUTES)
                    .isBefore(LocalDateTime.now())) {

                loginSucceeded(user);
                return true;
            }
        }
        return false;
    }
}
