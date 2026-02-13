package com.auth.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

import javax.management.relation.Role;


    @Entity
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Table(name = "users")

    public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(unique = true)
        private String email;

        @Column(unique = true)
        private String phone;

        private String password;

        @Enumerated(EnumType.STRING)
        private Role role;

        private boolean enabled;
    }


