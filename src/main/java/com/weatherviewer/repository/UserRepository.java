package com.weatherviewer.repository;

import com.weatherviewer.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link User}.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /** Looks up an account by its (unique) email/login. */
    Optional<User> findByEmail(String email);

    /** Checks whether an account with this email already exists (used by uniqueness validation). */
    boolean existsByEmail(String email);

    /**
     * Overrides the default {@code findAll()} to eagerly fetch each user's
     * {@code locations} in the same query, avoiding N+1 lazy-loading queries
     * when listing all users (e.g. in an admin view).
     */
    @Override
    @EntityGraph(attributePaths = {"locations"})
    List<User> findAll();

}
