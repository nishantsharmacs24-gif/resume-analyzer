package com.resumeanalyzer.repository;

import com.resumeanalyzer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// JpaRepository<User, Long> gives us:
// - save(), findById(), findAll(), deleteById() automatically!
// We just need to add our custom queries

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring auto-generates SQL: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // Check if email already registered
    boolean existsByEmail(String email);
}
