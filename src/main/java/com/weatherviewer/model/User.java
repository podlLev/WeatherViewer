package com.weatherviewer.model;

import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.List;

/**
 * A registered account of the application.
 * <p>
 * Holds login credentials, profile fields, the account's {@link Role}
 * (which drives Spring Security authorities) and lifecycle {@link UserStatus},
 * plus the collection of {@link Location}s the user has saved. Deleting a
 * user cascades to and removes all of their saved locations.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "users")
public class User extends BaseEntity {

    /** Unique login identifier for the account. */
    @Column(unique = true)
    private String email;

    private String firstName;
    private String lastName;

    /** BCrypt-hashed password; never stored or exposed in plain text. */
    private String password;

    /** Lifecycle state of the account (pending, active, blocked, etc.). */
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "user_status")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private UserStatus status;

    /** Authorization role that determines the account's granted permissions. */
    @Enumerated(value = EnumType.STRING)
    @Column(columnDefinition = "role")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Role role;

    /** Locations saved by this user; removed automatically if the user is deleted. */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Location> locations;

    /**
     * Builds a display-friendly full name from first and last name,
     * tolerating either being blank or {@code null}.
     *
     * @return "First Last", trimmed; may be an empty string if both parts are blank
     */
    public String getFullName() {
        String first = (firstName == null || firstName.isBlank()) ? "" : firstName;
        String last = (lastName == null || lastName.isBlank()) ? "" : lastName;

        return (first + " " + last).trim();
    }

}
