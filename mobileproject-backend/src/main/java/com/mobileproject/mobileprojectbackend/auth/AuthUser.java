package com.mobileproject.mobileprojectbackend.auth;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Document(collection = "users")
public class AuthUser {

    @Id
    private String id;
    private String username;
    private String email;
    private String passwordHash;
    private String createdAt;
    private String fullName;
    private String nickName;
    private String birthDate;
    private String gender;
    private boolean profileCompleted;
    private String partnerUserId;

    public AuthUser() {
    }

    public AuthUser(String username, String email, String passwordHash, String createdAt) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.profileCompleted = false;
    }

}
