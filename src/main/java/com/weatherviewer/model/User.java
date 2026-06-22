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

@Entity
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "users")
public class User extends BaseEntity {

    @Column(unique = true)
    private String email;

    private String firstName;
    private String lastName;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "user_status")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private UserStatus status;

    @Enumerated(value = EnumType.STRING)
    @Column(columnDefinition = "role")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Location> locations;

    public String getFullName() {
        String first = (firstName == null || firstName.isBlank()) ? "" : firstName;
        String last = (lastName == null || lastName.isBlank()) ? "" : lastName;

        return (first + " " + last).trim();
    }

}
