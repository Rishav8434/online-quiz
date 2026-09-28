package com.onlinequiz.repository;

import com.onlinequiz.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save and find user by username or email")
    void testSaveAndFindByUsername() {
        User user = new User("johndoe", "john@example.com", "hashpassword", "John Doe");
        userRepository.save(user);

        Optional<User> foundByUsername = userRepository.findByUsername("johndoe");
        assertTrue(foundByUsername.isPresent());
        assertEquals("john@example.com", foundByUsername.get().getEmail());

        Optional<User> foundByEmail = userRepository.findByEmail("john@example.com");
        assertTrue(foundByEmail.isPresent());
        assertEquals("johndoe", foundByEmail.get().getUsername());

        assertTrue(userRepository.existsByUsername("johndoe"));
        assertTrue(userRepository.existsByEmail("john@example.com"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }
}
