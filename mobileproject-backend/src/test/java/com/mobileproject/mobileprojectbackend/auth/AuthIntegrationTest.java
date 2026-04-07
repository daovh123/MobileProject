package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.LoginRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Autowired
    private CoupleRequestRepository coupleRequestRepository;

    @Test
    void profileGetPutFlowShouldPersistUpdatedProfile() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "it_profile_" + suffix;
        String email = username + "@example.com";
        String normalizedUsername = username.toLowerCase(Locale.ROOT);

        try {
            String token = registerAndLogin(username, email);

            mockMvc.perform(get("/api/auth/profile")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.username").value(normalizedUsername));

            Map<String, Object> payload = new HashMap<>();
            payload.put("fullName", "Profile User");
            payload.put("nickName", "PUser");
            payload.put("birthDate", "2020-02-02");
            payload.put("gender", "male");

            mockMvc.perform(put("/api/auth/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.fullName").value("Profile User"))
                    .andExpect(jsonPath("$.nickName").value("PUser"))
                    .andExpect(jsonPath("$.birthDate").value("2020-02-02"))
                    .andExpect(jsonPath("$.gender").value("MALE"))
                    .andExpect(jsonPath("$.profileCompleted").value(true));

            mockMvc.perform(get("/api/auth/profile")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("Profile User"))
                    .andExpect(jsonPath("$.gender").value("MALE"));
        } finally {
            cleanupUser(normalizedUsername);
        }
    }

    @Test
    void profileGetWithInvalidTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profilePutValidationFullNameRequiredReturnsBadRequest() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "it_profile_invalid_" + suffix;
        String email = username + "@example.com";
        String normalizedUsername = username.toLowerCase(Locale.ROOT);

        try {
            String token = registerAndLogin(username, email);

            Map<String, Object> payload = new HashMap<>();
            payload.put("fullName", "   ");
            payload.put("nickName", "N");
            payload.put("birthDate", "2020-02-02");
            payload.put("gender", "male");

            mockMvc.perform(put("/api/auth/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());
        } finally {
            cleanupUser(normalizedUsername);
        }
    }

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
                    .andExpect(jsonPath("$.username").value(normalizedUsername))
                    .andExpect(jsonPath("$.profileCompleted").value(false));
        } finally {
            cleanupUser(normalizedUsername);
        }
    }

    @Test
    void profileAndCoupleAcceptFlowShouldNotifyBothSides() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String userA = "it_a_" + suffix;
        String userB = "it_b_" + suffix;
        String emailA = userA + "@example.com";
        String emailB = userB + "@example.com";

        String normalizedUserA = userA.toLowerCase(Locale.ROOT);
        String normalizedUserB = userB.toLowerCase(Locale.ROOT);

        try {
            String tokenA = registerAndLogin(userA, emailA);
            String tokenB = registerAndLogin(userB, emailB);

            upsertProfile(tokenA, "User A", "Alpha", "01/02/2020", "female");
            upsertProfile(tokenB, "User B", "Beta", "2020-02-02", "male");

            String codeA = generateCode(tokenA);
            assertNotNull(codeA);

            MvcResult createRequestResult = mockMvc.perform(post("/api/auth/couple/requests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + tokenB)
                    .content(objectMapper.writeValueAsString(Map.of("partnerCode", codeA))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andReturn();

            String requestId = readJson(createRequestResult).path("requestId").asText();

            mockMvc.perform(get("/api/auth/couple/status")
                    .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.incomingRequestId").value(requestId))
                    .andExpect(jsonPath("$.incomingRequesterUsername").value(normalizedUserB));

            mockMvc.perform(post("/api/auth/couple/requests/{requestId}/decision", requestId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + tokenA)
                    .content(objectMapper.writeValueAsString(Map.of("accept", true))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACCEPTED"));

            mockMvc.perform(get("/api/auth/couple/status")
                    .header("Authorization", "Bearer " + tokenB))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paired").value(true))
                    .andExpect(jsonPath("$.partnerUsername").value(normalizedUserA))
                    .andExpect(jsonPath("$.outgoingStatus").value("ACCEPTED"));
        } finally {
            cleanupUser(normalizedUserA);
            cleanupUser(normalizedUserB);
        }
    }

    @Test
    void coupleRejectFlowShouldExposeRejectedStatusToRequester() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String userA = "it_reject_a_" + suffix;
        String userB = "it_reject_b_" + suffix;
        String emailA = userA + "@example.com";
        String emailB = userB + "@example.com";

        String normalizedUserA = userA.toLowerCase(Locale.ROOT);
        String normalizedUserB = userB.toLowerCase(Locale.ROOT);

        try {
            String tokenA = registerAndLogin(userA, emailA);
            String tokenB = registerAndLogin(userB, emailB);

            upsertProfile(tokenA, "Reject A", null, "2020-01-01", "other");
            upsertProfile(tokenB, "Reject B", null, "2020-01-02", "female");

            String codeA = generateCode(tokenA);

            MvcResult createRequestResult = mockMvc.perform(post("/api/auth/couple/requests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + tokenB)
                    .content(objectMapper.writeValueAsString(Map.of("partnerCode", codeA))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andReturn();

            String requestId = readJson(createRequestResult).path("requestId").asText();

            mockMvc.perform(post("/api/auth/couple/requests/{requestId}/decision", requestId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + tokenA)
                    .content(objectMapper.writeValueAsString(Map.of("accept", false))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REJECTED"));

            mockMvc.perform(get("/api/auth/couple/status")
                    .header("Authorization", "Bearer " + tokenB))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paired").value(false))
                    .andExpect(jsonPath("$.outgoingStatus").value("REJECTED"));
        } finally {
            cleanupUser(normalizedUserA);
            cleanupUser(normalizedUserB);
        }
    }

    @Test
    void logoutShouldInvalidateCurrentAccessToken() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "it_logout_" + suffix;
        String email = username + "@example.com";
        String normalizedUsername = username.toLowerCase(Locale.ROOT);

        try {
            String token = registerAndLogin(username, email);

            mockMvc.perform(post("/api/auth/logout")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            mockMvc.perform(get("/api/auth/profile")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        } finally {
            cleanupUser(normalizedUsername);
        }
    }

    private String registerAndLogin(String username, String email) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(username, email, "123456");
        LoginRequest loginRequest = new LoginRequest(username, "123456");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String token = readJson(loginResult).path("token").asText();
        assertNotNull(token);
        assertFalse(token.isBlank());
        return token;
    }

    private void upsertProfile(String token,
            String fullName,
            String nickName,
            String birthDate,
            String gender) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fullName", fullName);
        payload.put("nickName", nickName);
        payload.put("birthDate", birthDate);
        payload.put("gender", gender);

        mockMvc.perform(put("/api/auth/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gender").isNotEmpty())
                .andExpect(jsonPath("$.profileCompleted").value(true));
    }

    private String generateCode(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/couple/code")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.myCodeExpiresAt").isNotEmpty())
                .andReturn();

        String code = readJson(result).path("myCode").asText();
        assertNotNull(code);
        assertTrue(code.matches("\\d{3}-\\d{3}"));
        return code;
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private void cleanupUser(String normalizedUsername) {
        if (normalizedUsername == null || normalizedUsername.isBlank()) {
            return;
        }
        authUserRepository.deleteByUsername(normalizedUsername);
        coupleRequestRepository.deleteByRequesterUsername(normalizedUsername);
        coupleRequestRepository.deleteByRecipientUsername(normalizedUsername);
    }
}
