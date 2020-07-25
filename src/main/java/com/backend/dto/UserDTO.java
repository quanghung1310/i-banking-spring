package com.backend.dto;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name="user_banks")
public class UserDTO {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(length = 100)
    private String name;

    @Column(length = 11)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String userName;

    @Column(length = 15)
    private String password;

    @Column(length = 15)
    private String lastPassword;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    @Column(length = 100)
    private String description;

    @Column(length = 20)
    private String role;
}
