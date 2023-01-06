package com.secjar.secjarapi.models;

import com.secjar.secjarapi.enums.UserRolesEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "roles")
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private UserRolesEnum role;

    @ManyToMany(mappedBy = "roles")
    private List<User> users;

    public UserRole(UserRolesEnum role) {
        this.role = role;
    }
}
