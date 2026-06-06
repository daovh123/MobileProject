package com.mobileproject.mobileprojectbackend.payout;

import com.mobileproject.mobileprojectbackend.auth.CoupleInfoRepository;
import com.mobileproject.mobileprojectbackend.topup.SePayProperties;
import com.mobileproject.mobileprojectbackend.topup.dto.SePayWebhookRequest;
import com.mobileproject.mobileprojectbackend.transaction.TransactionService;
import com.mobileproject.mobileprojectbackend.transaction.TransactionType;
import com.mobileproject.mobileprojectbackend.transaction.dto.TransactionResponse;
import com.mobileproject.mobileprojectbackend.payout.dto.CreatePayoutRequest;
import com.mobileproject.mobileprojectbackend.payout.dto.PayoutResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Service xử lý logic nghiệp vụ rút tiền (payout) từ ví chung ra ngân hàng qua SePay.
 *
 * <p><b>Nghiệp vụ chính:</b></p>
 * <ul>
 *   <li>Tạo yêu cầu rút tiền với xác nhận số dư đủ</li>
 *   <li>Xử lý webhook từ SePay để xác nhận chuyển tiền thành công</li>
 * </ul>
 *
 * <p><b>Xử lý webhook (idempotent):</b></p>
 * <ol>
 *   <li>Chỉ chấp nhận giao dịch outgoing ({@code transferType = "out"})</li>
 *   <li>Kiểm tra deduplication qua sepayId/referenceCode</li>
 *   <li>Tìm yêu cầu phù hợp qua transferCode trong code/content/description</li>
 *   <li>Verify số tiền khớp</li>
 *   <li>Atomic claim: PENDING → PROCESSING (dùng {@code findAndModify})</li>
 *   <li>Gọi {@link TransactionService#saveTransaction} để ghi giao dịch EXPENSE</li>
 *   <li>Nếu thành công → PAID; nếu thất bại → release claim về PENDING</li>
 * </ol>
 */
@Service
public class PayoutService {

    private static final int MAX_TRANSFER_CODE_ATTEMPTS = 5;

    private final PayoutRequestRepository payoutRequestRepository;
    private final CoupleInfoRepository coupleInfoRepository;
    private final TransactionService transactionService;
    private final MongoTemplate mongoTemplate;
    private final SePayProperties sePayProperties;

    public PayoutService(PayoutRequestRepository payoutRequestRepository,
                         CoupleInfoRepository coupleInfoRepository,
                         TransactionService transactionService,
                         MongoTemplate mongoTemplate,
                         SePayProperties sePayProperties) {
        this.payoutRequestRepository = payoutRequestRepository;
        this.coupleInfoRepository = coupleInfoRepository;
        this.transactionService = transactionService;
        this.mongoTemplate = mongoTemplate;
        this.sePayProperties = sePayProperties;
    }

    /**
     * Tạo yêu cầu rút tiền mới.
     *
     * <p><b>Luồng xử lý:</b></p>
     * <ol>
     *   <li>Validate coupleId, amount</li>
     *   <li>Kiểm tra cặp đôi tồn tại và số dư đủ</li>
     *   <li>Sinh mã chuyển khoản duy nhất (prefix + UUID, retry tối đa 5 lần)</li>
     *   <li>Lưu yêu cầu vào collection {@code payout_requests}</li>
     * </ol>
     *
     * @param request {@link CreatePayoutRequest} chứa coupleId, amount
     * @return {@link PayoutResponse} chứa thông tin yêu cầu; hoặc thông báo lỗi
     */
    public PayoutResponse createPayoutRequest(CreatePayoutRequest request) {
        if (request.coupleId() == null || request.coupleId().isBlank()) {
            return PayoutResponse.failure("Couple ID is required");
        }
        if (request.amount() == null || request.amount() <= 0) {
            return PayoutResponse.failure("Amount must be positive");
        }
        Long currentBalance = coupleInfoRepository.findById(request.coupleId())
                .map(couple -> couple.getTotalBalance())
                .orElse(null);
        if (currentBalance == null) {
            return PayoutResponse.failure("Couple not found");
        }
        if (currentBalance < request.amount()) {
            return PayoutResponse.failure("Insufficient balance");
        }

        for (int attempt = 0; attempt < MAX_TRANSFER_CODE_ATTEMPTS; attempt++) {
            String transferCode = generateTransferCode();
            PayoutRequest payoutRequest = new PayoutRequest(request.coupleId(), request.amount(), transferCode);
            try {
                PayoutRequest saved = payoutRequestRepository.save(payoutRequest);
                return toResponse(saved);
            } catch (DuplicateKeyException ignored) {
                // retry with another code
            }
        }
        return PayoutResponse.failure("Could not generate a unique transfer code");
    }

    /**
     * Tìm và trả về thông tin yêu cầu rút tiền theo ID.
     *
     * @param id ID yêu cầu rút tiền
     * @return {@link PayoutResponse} nếu tìm thấy
     */
    public Optional<PayoutResponse> findPayoutRequest(String id) {
        return payoutRequestRepository.findById(id).map(this::toResponse);
    }

    /**
     * Xử lý webhook từ SePay khi có giao dịch chuyển khoản đi (payout confirmed).
     *
     * <p><b>Luồng xử lý:</b></p>
     * <ol>
     *   <li>Chỉ chấp nhận {@code transferType = "out"}</li>
     *   <li>Kiểm tra deduplication qua sepayId/referenceCode</li>
     *   <li>Tìm yêu cầu phù hợp qua transferCode</li>
     *   <li>Verify số tiền khớp</li>
     *   <li>Atomic claim: PENDING → PROCESSING</li>
     *   <li>Ghi giao dịch EXPENSE vào hệ thống</li>
     *   <li>Nếu thành công → PAID; nếu thất bại → release claim</li>
     * </ol>
     *
     * @param webhook {@link SePayWebhookRequest} từ SePay
     * @return {@link PayoutWebhookResult} kết quả xử lý
     */
    public PayoutWebhookResult handleSePayWebhook(SePayWebhookRequest webhook) {
        if (webhook == null || webhook.id() == null) {
            return PayoutWebhookResult.failure("Missing SePay transaction id");
        }
        if (!"out".equalsIgnoreCase(nullToEmpty(webhook.transferType()))) {
            return PayoutWebhookResult.failure("Only outgoing SePay transfers can confirm payout");
        }
        if (webhook.transferAmount() == null || webhook.transferAmount() <= 0) {
            return PayoutWebhookResult.failure("Transfer amount must be positive");
        }

        Optional<PayoutRequest> processedBySepayId = payoutRequestRepository.findBySepayId(webhook.id());
        if (processedBySepayId.filter(this::isPaid).isPresent()) {
            return PayoutWebhookResult.success("Duplicate webhook already processed");
        }
        if (hasText(webhook.referenceCode())) {
            Optional<PayoutRequest> processedByReference = payoutRequestRepository.findByReferenceCode(webhook.referenceCode());
            if (processedByReference.filter(this::isPaid).isPresent()) {
                return PayoutWebhookResult.success("Duplicate webhook already processed");
            }
        }

        Optional<PayoutRequest> matchedRequest = findMatchingRequest(webhook);
        if (matchedRequest.isEmpty()) {
            return PayoutWebhookResult.failure("No pending payout request matched this webhook");
        }
        PayoutRequest pendingRequest = matchedRequest.get();

        if (!pendingRequest.getAmount().equals(webhook.transferAmount())) {
            return PayoutWebhookResult.failure("Webhook amount does not match payout request amount");
        }

        PayoutRequest claimedRequest = claimPendingRequest(pendingRequest, webhook);
        if (claimedRequest == null) {
            Optional<PayoutRequest> alreadyPaid = payoutRequestRepository.findByTransferCode(pendingRequest.getTransferCode())
                    .filter(this::isPaid);
            if (alreadyPaid.isPresent()) {
                return PayoutWebhookResult.success("Duplicate webhook already processed");
            }
            return PayoutWebhookResult.failure("Payout request is not pending");
        }

        TransactionResponse transactionResponse = transactionService.saveTransaction(
                claimedRequest.getCoupleId(),
                claimedRequest.getAmount(),
                TransactionType.EXPENSE,
                "PAYOUT",
                "SePay payout " + claimedRequest.getTransferCode()
        );
        if (!transactionResponse.success()) {
            releaseClaim(claimedRequest, transactionResponse.message());
            return PayoutWebhookResult.failure(transactionResponse.message());
        }

        markPaid(claimedRequest, transactionResponse.transactionId());
        return PayoutWebhookResult.success("Payout processed");
    }

    /**
     * Chuyển đổi entity thành response DTO.
     */
    private PayoutResponse toResponse(PayoutRequest request) {
        Long currentBalance = coupleInfoRepository.findById(request.getCoupleId())
                .map(couple -> couple.getTotalBalance())
                .orElse(null);
        return new PayoutResponse(
                true,
                "Payout request created",
                request.getId(),
                request.getCoupleId(),
                request.getAmount(),
                request.getStatus().name(),
                request.getTransferCode(),
                currentBalance,
                request.getCreatedAt(),
                request.getPaidAt()
        );
    }

    /**
     * Tìm yêu cầu rút tiền phù hợp với webhook.
     * Ưu tiên match theo code, sau đó tìm trong content/description.
     */
    private Optional<PayoutRequest> findMatchingRequest(SePayWebhookRequest webhook) {
        if (hasText(webhook.code())) {
            Optional<PayoutRequest> byCode = payoutRequestRepository.findByTransferCode(webhook.code().trim());
            if (byCode.isPresent()) {
                return byCode;
            }
        }
        String searchableText = (nullToEmpty(webhook.content()) + " " + nullToEmpty(webhook.description()))
                .toUpperCase(Locale.ROOT);
        if (searchableText.isBlank()) {
            return Optional.empty();
        }
        String prefix = hasText(sePayProperties.transferPrefix())
                ? sePayProperties.transferPrefix().toUpperCase(Locale.ROOT)
                : "YMW";
        return payoutRequestRepository.findAll().stream()
                .filter(request -> request.getStatus() == PayoutRequestStatus.PENDING
                        || request.getStatus() == PayoutRequestStatus.PAID)
                .filter(request -> request.getTransferCode() != null
                        && request.getTransferCode().toUpperCase(Locale.ROOT).startsWith(prefix))
                .filter(request -> searchableText.contains(request.getTransferCode().toUpperCase(Locale.ROOT)))
                .findFirst();
    }

    /**
     * Atomic claim: chuyển trạng thái PENDING → PROCESSING chỉ khi request vẫn đang PENDING
     * và số tiền khớp. Sử dụng findAndModify để đảm bảo chỉ một webhook xử lý được.
     *
     * @return request đã được claim (PROCESSING), hoặc null nếu không claim được
     */
    private PayoutRequest claimPendingRequest(PayoutRequest request, SePayWebhookRequest webhook) {
        Query query = new Query(Criteria.where("id").is(request.getId())
                .and("status").is(PayoutRequestStatus.PENDING)
                .and("amount").is(webhook.transferAmount()));
        Update update = new Update()
                .set("status", PayoutRequestStatus.PROCESSING)
                .set("sepayId", webhook.id())
                .set("referenceCode", emptyToNull(webhook.referenceCode()))
                .set("webhookContent", webhook.content())
                .set("webhookDescription", webhook.description())
                .set("updatedAt", Instant.now())
                .unset("lastError");
        return mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), PayoutRequest.class);
    }

    /**
     * Release claim: hoàn tác PROCESSING → PENDING khi ghi giao dịch thất bại.
     */
    private void releaseClaim(PayoutRequest request, String error) {
        Query query = new Query(Criteria.where("id").is(request.getId())
                .and("status").is(PayoutRequestStatus.PROCESSING));
        Update update = new Update()
                .set("status", PayoutRequestStatus.PENDING)
                .set("lastError", error)
                .set("updatedAt", Instant.now())
                .unset("sepayId")
                .unset("referenceCode");
        mongoTemplate.updateFirst(query, update, PayoutRequest.class);
    }

    /**
     * Đánh dấu yêu cầu rút tiền đã thanh toán: PROCESSING → PAID.
     */
    private void markPaid(PayoutRequest request, String transactionId) {
        Query query = new Query(Criteria.where("id").is(request.getId())
                .and("status").is(PayoutRequestStatus.PROCESSING));
        Update update = new Update()
                .set("status", PayoutRequestStatus.PAID)
                .set("transactionId", transactionId)
                .set("paidAt", Instant.now())
                .set("updatedAt", Instant.now());
        mongoTemplate.updateFirst(query, update, PayoutRequest.class);
    }

    /**
     * Sinh mã chuyển khoản duy nhất: prefix + 10 ký tự UUID uppercase.
     */
    private String generateTransferCode() {
        String prefix = hasText(sePayProperties.transferPrefix()) ? sePayProperties.transferPrefix() : "YMW";
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
        return prefix.toUpperCase(Locale.ROOT) + suffix;
    }

    private boolean isPaid(PayoutRequest request) {
        return request.getStatus() == PayoutRequestStatus.PAID;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String emptyToNull(String value) {
        return hasText(value) ? value : null;
    }
}

