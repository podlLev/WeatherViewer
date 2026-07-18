package com.weatherviewer.repository;

import com.weatherviewer.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Paginated counterpart to {@link #findAll()} with the same eager
     * {@code locations} fetch. Used by the admin user list so it can page
     * through the full user base instead of loading every account (and
     * every account's locations) into memory in one response.
     */
    @Override
    @EntityGraph(attributePaths = {"locations"})
    Page<User> findAll(Pageable pageable);

}
