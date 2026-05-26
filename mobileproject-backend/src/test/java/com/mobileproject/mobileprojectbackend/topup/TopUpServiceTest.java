package com.mobileproject.mobileprojectbackend.topup;

import com.mobileproject.mobileprojectbackend.auth.CoupleInfo;
import com.mobileproject.mobileprojectbackend.auth.CoupleInfoRepository;
import com.mobileproject.mobileprojectbackend.transaction.TransactionService;
import com.mobileproject.mobileprojectbackend.transaction.TransactionType;
import com.mobileproject.mobileprojectbackend.transaction.dto.TransactionResponse;
import com.mobileproject.mobileprojectbackend.topup.dto.CreateTopUpRequest;
import com.mobileproject.mobileprojectbackend.topup.dto.SePayWebhookRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TopUpServiceTest {

    @Mock
    private TopUpRequestRepository topUpRequestRepository;

    @Mock
    private CoupleInfoRepository coupleInfoRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private SePayProperties sePayProperties;

    @InjectMocks
    private TopUpService topUpService;

    @Test
    void createTopUpRequestReturnsTransferDetailsAndQrUrl() {
        CoupleInfo couple = new CoupleInfo();
        couple.setId("couple-1");

        when(coupleInfoRepository.findById("couple-1")).thenReturn(Optional.of(couple));
        stubSePayDefaults();
        when(topUpRequestRepository.save(any(TopUpRequest.class))).thenAnswer(invocation -> {
            TopUpRequest request = invocation.getArgument(0);
            request.setId("topup-1");
            return request;
        });

        var response = topUpService.createTopUpRequest(new CreateTopUpRequest(
                "couple-1",
                120000L,
                "MBBank",
                "Demo Bank",
                null));

        assertTrue(response.success());
        assertEquals("couple-1", response.coupleId());
        assertEquals(120000L, response.amount());
        assertEquals("PENDING", response.status());
        assertTrue(response.transferCode().startsWith("YMW"));
        assertEquals(response.transferContent(), response.qrContent());
        assertTrue(response.qrImageUrl().startsWith("https://qr.sepay.vn/img?"));
        assertTrue(response.qrImageUrl().contains("amount=120000"));
    }

    @Test
    void handleSePayWebhookProcessesIncomingMatchingPendingTopUp() {
        stubSePayDefaults();
        TopUpRequest pending = pendingTopUp();
        TopUpRequest claimed = pendingTopUp();
        claimed.setStatus(TopUpRequestStatus.PROCESSING);
        claimed.setSepayId(9876L);
        claimed.setReferenceCode("FT123");

        when(topUpRequestRepository.findBySepayId(9876L)).thenReturn(Optional.empty());
        when(topUpRequestRepository.findByReferenceCode("FT123")).thenReturn(Optional.empty());
        when(topUpRequestRepository.findByTransferCode("YMW123456")).thenReturn(Optional.of(pending));
        when(mongoTemplate.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(TopUpRequest.class))).thenReturn(claimed);
        when(transactionService.saveTransaction(
                eq("couple-1"),
                eq(150000L),
                eq(TransactionType.INCOME),
                eq("INCOME"),
                eq("SePay top-up YMW123456")))
                .thenReturn(TransactionResponse.success("txn-1", 150000L, "INCOME", "INCOME",
                        "SePay top-up YMW123456", 650000L));

        TopUpWebhookResult result = topUpService.handleSePayWebhook(webhook("in", 150000L));

        assertTrue(result.success());
        verify(transactionService).saveTransaction(
                eq("couple-1"),
                eq(150000L),
                eq(TransactionType.INCOME),
                eq("INCOME"),
                eq("SePay top-up YMW123456"));
        verify(mongoTemplate).updateFirst(any(Query.class), any(Update.class), eq(TopUpRequest.class));
    }

    @Test
    void handleSePayWebhookReturnsSuccessForAlreadyPaidDuplicate() {
        TopUpRequest paid = pendingTopUp();
        paid.setStatus(TopUpRequestStatus.PAID);
        paid.setSepayId(9876L);

        when(topUpRequestRepository.findBySepayId(9876L)).thenReturn(Optional.of(paid));

        TopUpWebhookResult result = topUpService.handleSePayWebhook(webhook("in", 150000L));

        assertTrue(result.success());
        verify(transactionService, never()).saveTransaction(any(), any(), any(), any(), any());
    }

    @Test
    void handleSePayWebhookRejectsAmountMismatch() {
        stubSePayDefaults();
        TopUpRequest pending = pendingTopUp();

        when(topUpRequestRepository.findBySepayId(9876L)).thenReturn(Optional.empty());
        when(topUpRequestRepository.findByReferenceCode("FT123")).thenReturn(Optional.empty());
        when(topUpRequestRepository.findByTransferCode("YMW123456")).thenReturn(Optional.of(pending));

        TopUpWebhookResult result = topUpService.handleSePayWebhook(webhook("in", 140000L));

        assertEquals(false, result.success());
        verify(transactionService, never()).saveTransaction(any(), any(), any(), any(), any());
        verify(mongoTemplate, never()).findAndModify(any(Query.class), any(Update.class),
                any(FindAndModifyOptions.class), eq(TopUpRequest.class));
    }

    @Test
    void handleSePayWebhookRejectsOutgoingTransfer() {
        stubSePayDefaults();

        TopUpWebhookResult result = topUpService.handleSePayWebhook(webhook("out", 150000L));

        assertEquals(false, result.success());
        verify(transactionService, never()).saveTransaction(any(), any(), any(), any(), any());
    }

    @Test
    void handleSePayWebhookClaimsPendingRequestBeforeSavingTransaction() {
        stubSePayDefaults();
        TopUpRequest pending = pendingTopUp();
        TopUpRequest claimed = pendingTopUp();
        claimed.setStatus(TopUpRequestStatus.PROCESSING);

        when(topUpRequestRepository.findBySepayId(9876L)).thenReturn(Optional.empty());
        when(topUpRequestRepository.findByReferenceCode("FT123")).thenReturn(Optional.empty());
        when(topUpRequestRepository.findByTransferCode("YMW123456")).thenReturn(Optional.of(pending));
        when(mongoTemplate.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(TopUpRequest.class))).thenReturn(claimed);
        when(transactionService.saveTransaction(any(), any(), any(), any(), any()))
                .thenReturn(TransactionResponse.success("txn-1", 150000L, "INCOME", "INCOME",
                        "SePay top-up YMW123456", 650000L));

        topUpService.handleSePayWebhook(webhook("in", 150000L));

        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).findAndModify(any(Query.class), updateCaptor.capture(),
                any(FindAndModifyOptions.class), eq(TopUpRequest.class));
        assertNotNull(updateCaptor.getValue());
    }

    private void stubSePayDefaults() {
        lenient().when(sePayProperties.bankCode()).thenReturn("MBBank");
        lenient().when(sePayProperties.bankName()).thenReturn("Demo Bank");
        lenient().when(sePayProperties.accountNumber()).thenReturn("0123456789");
        lenient().when(sePayProperties.accountName()).thenReturn("YOU AND ME WALLET");
        lenient().when(sePayProperties.transferPrefix()).thenReturn("YMW");
        lenient().when(sePayProperties.qrTemplate()).thenReturn("compact");
    }

    private TopUpRequest pendingTopUp() {
        TopUpRequest request = new TopUpRequest(
                "couple-1",
                150000L,
                "YMW123456",
                "MBBank",
                "Demo Bank",
                "0123456789",
                "YOU AND ME WALLET",
                "YMW123456 NAP VI CHUNG",
                "YMW123456 NAP VI CHUNG",
                "https://qr.sepay.vn/img?acc=0123456789");
        request.setId("topup-1");
        request.setCreatedAt(Instant.now());
        return request;
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
