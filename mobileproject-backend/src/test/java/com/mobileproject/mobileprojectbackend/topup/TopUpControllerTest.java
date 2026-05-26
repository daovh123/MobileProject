package com.mobileproject.mobileprojectbackend.topup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobileproject.mobileprojectbackend.topup.dto.CreateTopUpRequest;
import com.mobileproject.mobileprojectbackend.topup.dto.SePayWebhookRequest;
import com.mobileproject.mobileprojectbackend.topup.dto.TopUpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TopUpControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TopUpService topUpService;

    @Mock
    private SePayProperties sePayProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(new TopUpController(topUpService, sePayProperties)).build();
    }

    @Test
    void createTopUpRequestReturnsPaymentDetails() throws Exception {
        TopUpResponse response = new TopUpResponse(
                true,
                "Top-up request created",
                "topup-1",
                "couple-1",
                150000L,
                "PENDING",
                "YMW123456",
                "MBBank",
                "Demo Bank",
                "0123456789",
                "YOU AND ME WALLET",
                "YMW123456 NAP VI CHUNG",
                "YMW123456 NAP VI CHUNG",
                "https://qr.sepay.vn/img?acc=0123456789",
                null,
                Instant.now(),
                null);

        when(topUpService.createTopUpRequest(any(CreateTopUpRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/top-ups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTopUpRequest(
                                "couple-1",
                                150000L,
                                "MBBank",
                                "Demo Bank",
                                "demo"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transferCode").value("YMW123456"))
                .andExpect(jsonPath("$.accountNumber").value("0123456789"))
                .andExpect(jsonPath("$.qrImageUrl").isNotEmpty());
    }

    @Test
    void getTopUpRequestReturnsStatus() throws Exception {
        TopUpResponse response = new TopUpResponse(
                true,
                "Top-up request created",
                "topup-1",
                "couple-1",
                150000L,
                "PAID",
                "YMW123456",
                "MBBank",
                "Demo Bank",
                "0123456789",
                "YOU AND ME WALLET",
                "YMW123456 NAP VI CHUNG",
                "YMW123456 NAP VI CHUNG",
                "https://qr.sepay.vn/img?acc=0123456789",
                650000L,
                Instant.now(),
                Instant.now());

        when(topUpService.findTopUpRequest("topup-1")).thenReturn(java.util.Optional.of(response));

        mockMvc.perform(get("/api/v1/top-ups/topup-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("topup-1"))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.currentBalance").value(650000));
    }

    @Test
    void webhookReturnsSuccessTrueWhenServiceAcceptsPayload() throws Exception {
        when(sePayProperties.apiKey()).thenReturn("");
        when(topUpService.handleSePayWebhook(any(SePayWebhookRequest.class)))
                .thenReturn(TopUpWebhookResult.success("Top-up processed"));

        mockMvc.perform(post("/api/v1/top-ups/sepay/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhook("in", 150000L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void webhookReturnsBadRequestWhenServiceRejectsPayload() throws Exception {
        when(sePayProperties.apiKey()).thenReturn("");
        when(topUpService.handleSePayWebhook(any(SePayWebhookRequest.class)))
                .thenReturn(TopUpWebhookResult.failure("Amount mismatch"));

        mockMvc.perform(post("/api/v1/top-ups/sepay/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhook("in", 140000L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void webhookReturnsUnauthorizedWhenApiKeyIsConfiguredAndHeaderDoesNotMatch() throws Exception {
        when(sePayProperties.apiKey()).thenReturn("secret-key");

        mockMvc.perform(post("/api/v1/top-ups/sepay/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Apikey wrong-key")
                        .content(objectMapper.writeValueAsString(webhook("in", 150000L))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        verify(topUpService, never()).handleSePayWebhook(any(SePayWebhookRequest.class));
    }

    @Test
    void webhookAcceptsConfiguredApiKeyHeader() throws Exception {
        when(sePayProperties.apiKey()).thenReturn("secret-key");
        when(topUpService.handleSePayWebhook(any(SePayWebhookRequest.class)))
                .thenReturn(TopUpWebhookResult.success("Top-up processed"));

        mockMvc.perform(post("/api/v1/top-ups/sepay/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Apikey secret-key")
                        .content(objectMapper.writeValueAsString(webhook("in", 150000L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private SePayWebhookRequest webhook(String transferType, Long amount) {
        return new SePayWebhookRequest(
                9876L,
                "MBBank",
                "2026-01-15 10:30:00",
                "0123456789",
                "",
                "YMW123456",
                "YMW123456 NAP VI CHUNG",
                transferType,
                "NGUYEN VAN A chuyen tien YMW123456",
                amount,
                5000000L,
                "FT123");
    }
}
