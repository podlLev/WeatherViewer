package com.weatherviewer.model;

import com.weatherviewer.model.enums.TokenType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;

/**
 * A single-use, expiring token used for either email verification or
 * password reset (see {@link TokenType}).
 * <p>
 * Tokens are opaque random strings (never the entity's own {@code id}) so
 * they can be safely embedded in an emailed URL. Each token is valid until
 * either {@link #expiresAt} passes or it is consumed once ({@link #used}).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "verification_tokens")
public class VerificationToken extends BaseEntity {

    /** Opaque, unique, unguessable token value embedded in the emailed link. */
    @Column(unique = true, nullable = false)
    private String token;

    /** The account this token grants an action for. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** What this token authorizes: confirming an email or resetting a password. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "token_type", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TokenType type;

    /** Moment after which the token can no longer be redeemed. */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** Whether the token has already been redeemed; consumed tokens can never be reused. */
    private boolean used;

    /** @return {@code true} if the token is still within its validity window and has not been redeemed yet. */
    public boolean isValid() {
        return !used && expiresAt.isAfter(LocalDateTime.now());
    }

}
