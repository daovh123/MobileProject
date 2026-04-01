package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.LoginRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.util.Locale;
import java.util.UUID;

import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Test
    void registerThenLoginShouldPersistUserInMongoDb() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "it_user_" + suffix;
        String email = username + "@example.com";
        String normalizedUsername = username.toLowerCase(Locale.ROOT);

        RegisterRequest registerRequest = new RegisterRequest(username, email, "123456");
        LoginRequest loginRequest = new LoginRequest(username, "123456");

        try {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.username").value(normalizedUsername))
                    .andExpect(jsonPath("$.email").value(email.toLowerCase(Locale.ROOT)));

            assertTrue(authUserRepository.existsByUsername(normalizedUsername));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.username").value(normalizedUsername));
        } finally {
            authUserRepository.deleteByUsername(normalizedUsername);
        }
    }
}
