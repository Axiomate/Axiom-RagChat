package com.axiom.RagChat.user.service;

import com.axiom.RagChat.exception.ApiException;
import com.axiom.RagChat.user.entity.User;
import com.axiom.RagChat.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void incrementSessionCount(Long userId) {
        User user = getUserById(userId);
        user.setTotalSessions(user.getTotalSessions() + 1);
        userRepository.save(user);
    }

    @Transactional
    public void incrementMessageCount(Long userId, int count) {
        User user = getUserById(userId);
        user.setTotalMessages(user.getTotalMessages() + count);
        userRepository.save(user);
    }

    @Transactional
    public void updatePreferenceSummary(Long userId, String preferenceSummary) {
        User user = getUserById(userId);
        user.setPreferenceSummary(preferenceSummary);
        userRepository.save(user);
    }
}
