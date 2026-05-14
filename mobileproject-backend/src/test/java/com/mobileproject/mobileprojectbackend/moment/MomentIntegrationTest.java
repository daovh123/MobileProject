package com.mobileproject.mobileprojectbackend.moment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.mobileproject.mobileprojectbackend.auth.AuthUserRepository;
import com.mobileproject.mobileprojectbackend.auth.CoupleInfoRepository;
import com.mobileproject.mobileprojectbackend.auth.CoupleRequestRepository;
import com.mobileproject.mobileprojectbackend.auth.dto.LoginRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.RegisterRequest;
import com.mobileproject.mobileprojectbackend.moment.comment.MomentCommentRepository;
import com.mobileproject.mobileprojectbackend.moment.reaction.MomentReactionRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(properties = "app.auth.token-secret=test-token-secret-for-spring-tests")
@AutoConfigureMockMvc
class MomentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private CoupleRequestRepository coupleRequestRepository;

    @Autowired
    private CoupleInfoRepository coupleInfoRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private MomentReactionRepository momentReactionRepository;

    @Autowired
    private MomentCommentRepository momentCommentRepository;

    @Test
    void momentCreateReactCommentFlowShouldSucceed() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String userA = "it_moment_a_" + suffix;
        String userB = "it_moment_b_" + suffix;
        String emailA = userA + "@example.com";
        String emailB = userB + "@example.com";

        String normalizedUserA = userA.toLowerCase(Locale.ROOT);
        String normalizedUserB = userB.toLowerCase(Locale.ROOT);

        String momentId = null;
        String coupleId = null;

        try {
            String tokenA = registerAndLogin(userA, emailA);
            String tokenB = registerAndLogin(userB, emailB);

            upsertProfile(tokenA, "Moment A", "Alpha", "2020-02-01", "female");
            upsertProfile(tokenB, "Moment B", "Beta", "2020-02-02", "male");

            String codeA = generateCode(tokenA);
            String requestId = createCoupleRequest(tokenB, codeA);
            acceptCoupleRequest(tokenA, requestId);

            coupleId = readJson(mockMvc.perform(get("/api/auth/couple/status")
                    .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andReturn()).path("coupleId").asString();

            assertNotNull(coupleId);

            Map<String, Object> payload = new HashMap<>();
            payload.put("coupleId", coupleId);
            payload.put("title", "First Moment");
            payload.put("base64Image", "data:image/jpeg;base64,AA==");

            MvcResult createResult = mockMvc.perform(post("/api/v1/moments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + tokenA)
                    .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.moment.id").isNotEmpty())
                    .andReturn();

            momentId = readJson(createResult).path("moment").path("id").asString();
            assertNotNull(momentId);

            mockMvc.perform(get("/api/v1/moments")
                    .param("coupleId", coupleId)
                    .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(momentId));

            mockMvc.perform(post("/api/v1/moments/{momentId}/reactions", momentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + tokenA)
                    .content(objectMapper.writeValueAsString(Map.of("reaction", "HEART"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reactionsCount").value(1))
                    .andExpect(jsonPath("$.viewerReaction").value("HEART"));

            mockMvc.perform(post("/api/v1/moments/{momentId}/comments", momentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + tokenA)
                    .content(objectMapper.writeValueAsString(Map.of("content", "Nice"))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.content").value("Nice"));

            mockMvc.perform(get("/api/v1/moments/{momentId}/comments", momentId)
                    .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].content").value("Nice"));
        } finally {
            cleanupMoment(momentId);
            cleanupCouple(coupleId);
            cleanupUser(normalizedUserA);
            cleanupUser(normalizedUserB);
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

        String token = readJson(loginResult).path("token").asString();
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
                .andExpect(jsonPath("$.profileCompleted").value(true));
    }

    private String generateCode(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/couple/code")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String code = readJson(result).path("myCode").asString();
        assertNotNull(code);
        assertTrue(code.matches("\\d{3}-\\d{3}"));
        return code;
    }

    private String createCoupleRequest(String token, String partnerCode) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/couple/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(Map.of("partnerCode", partnerCode))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        return readJson(result).path("requestId").asString();
    }

    private void acceptCoupleRequest(String token, String requestId) throws Exception {
        mockMvc.perform(post("/api/auth/couple/requests/{requestId}/decision", requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(Map.of("accept", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private void cleanupMoment(String momentId) {
        if (momentId == null || momentId.isBlank()) {
            return;
        }
        momentReactionRepository.findByMomentIdOrderByCreatedAtDesc(momentId)
                .forEach(reaction -> momentReactionRepository.deleteById(reaction.getId()));
        momentCommentRepository.findByMomentIdOrderByCreatedAtAsc(momentId)
                .forEach(comment -> momentCommentRepository.deleteById(comment.getId()));
        momentRepository.deleteById(momentId);
    }

    private void cleanupCouple(String coupleId) {
        if (coupleId == null || coupleId.isBlank()) {
            return;
        }
        coupleInfoRepository.deleteById(coupleId);
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
