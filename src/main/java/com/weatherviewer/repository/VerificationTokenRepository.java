package com.weatherviewer.repository;

import com.weatherviewer.model.VerificationToken;
import com.weatherviewer.model.enums.TokenType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByTokenAndType(String token, TokenType type);

    /** Invalidates any earlier, still-unused tokens of the given type for a user before a fresh one is issued. */
    @Modifying
    @Query("update VerificationToken t set t.used = true " +
            "where t.user.id = :userId and t.type = :type and t.used = false")
    void invalidateActiveTokens(@Param("userId") UUID userId, @Param("type") TokenType type);

}
