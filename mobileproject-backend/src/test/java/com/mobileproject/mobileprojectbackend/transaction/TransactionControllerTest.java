package com.mobileproject.mobileprojectbackend.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobileproject.mobileprojectbackend.transaction.dto.IncomeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
    }

    @Test
    void testProcessIncome_success() throws Exception {
        // Arrange
        IncomeRequest request = new IncomeRequest(
                "couple:test",
                100L,
                "WALLET",
                null,
                "note"
        );
        // Mock service response
        when(transactionService.processIncome(anyString(), anyLong(), anyString(), isNull(), anyString()))
                .thenReturn(
                        new com.mobileproject.mobileprojectbackend.transaction.dto.TransactionResponse(
                                true,
                                "Success",
                                "txnId",
                                100L,
                                "INCOME",
                                "INCOME",
                                "note",
                                500L,
                                java.time.Instant.now()
                        )
                );

        // Act & Assert
        mockMvc.perform(post("/api/v1/transactions/income")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.transactionId").value("txnId"))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.category").value("INCOME"))
                .andExpect(jsonPath("$.note").value("note"))
                .andExpect(jsonPath("$.currentBalance").value(500));
    }

    @Test
    void testProcessIncome_invalidJson() throws Exception {
        // Send malformed JSON
        mockMvc.perform(post("/api/v1/transactions/income")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }
}