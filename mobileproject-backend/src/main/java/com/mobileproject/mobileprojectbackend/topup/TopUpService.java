package com.mobileproject.mobileprojectbackend.topup;

import com.mobileproject.mobileprojectbackend.auth.CoupleInfoRepository;
import com.mobileproject.mobileprojectbackend.transaction.TransactionService;
import com.mobileproject.mobileprojectbackend.transaction.TransactionType;
import com.mobileproject.mobileprojectbackend.transaction.dto.TransactionResponse;
import com.mobileproject.mobileprojectbackend.topup.dto.CreateTopUpRequest;
import com.mobileproject.mobileprojectbackend.topup.dto.SePayWebhookRequest;
import com.mobileproject.mobileprojectbackend.topup.dto.TopUpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Service xử lý logic nghiệp vụ nạp tiền vào ví chung thông qua SePay.
 *
 * <p><b>Nghiệp vụ chính:</b></p>
 * <ul>
 *   <li>Tạo yêu cầu nạp tiền với mã chuyển khoản duy nhất và mã QR</li>
 *   <li>Xử lý webhook từ SePay để xác nhận thanh toán</li>
 * </ul>
 *
 * <p><b>Xử lý webhook (idempotent):</b></p>
 * <ol>
 *   <li>Kiểm tra trùng lặp qua {@code sepayId} hoặc {@code referenceCode}</li>
 *   <li>Chỉ chấp nhận giao dịch incoming ({@code transferType = "in"})</li>
 *   <li>Đối soát yêu cầu nạp tiền dựa trên mã chuyển khoản trong nội dung/description</li>
 *   <li>Verify số tiền khớp với yêu cầu gốc</li>
 *   <li>Atomic claim: sử dụng {@link MongoTemplate#findAndModify} chuyển trạng thái
 *       PENDING → PROCESSING (chỉ thành công nếu vẫn đang PENDING)</li>
 *   <li>Gọi {@link TransactionService#saveTransaction} để ghi giao dịch INCOME</li>
 *   <li>Nếu thành công → đánh dấu PAID; nếu thất bại → release claim về PENDING</li>
 * </ol>
 *
 * <p><b>Transfer code generation:</b> Sinh mã duy nhất với prefix (mặc định "YMW") + 10 ký tự UUID.
 * Có retry tối đa 5 lần nếu trùng lặp (rất hiếm).</p>
 */
@Service
public class TopUpService {

    private static final int MAX_TRANSFER_CODE_ATTEMPTS = 5;
    private static final Logger log = LoggerFactory.getLogger(TopUpService.class);

    private final TopUpRequestRepository topUpRequestRepository;
    private final CoupleInfoRepository coupleInfoRepository;
    private final TransactionService transactionService;
    private final MongoTemplate mongoTemplate;
    private final SePayProperties sePayProperties;

    public TopUpService(TopUpRequestRepository topUpRequestRepository,
                        CoupleInfoRepository coupleInfoRepository,
                        TransactionService transactionService,
                        MongoTemplate mongoTemplate,
                        SePayProperties sePayProperties) {
        this.topUpRequestRepository = topUpRequestRepository;
        this.coupleInfoRepository = coupleInfoRepository;
        this.transactionService = transactionService;
        this.mongoTemplate = mongoTemplate;
        this.sePayProperties = sePayProperties;
    }

    /**
     * Tạo yêu cầu nạp tiền mới.
     *
     * <p><b>Luồng xử lý:</b></p>
     * <ol>
     *   <li>Validate coupleId, amount và xác nhận cặp đôi tồn tại</li>
     *   <li>Sinh mã chuyển khoản duy nhất (prefix + UUID)</li>
     *   <li>Xây dựng nội dung chuyển khoản và URL mã QR</li>
     *   <li>Lưu yêu cầu vào collection {@code top_up_requests} (retry nếu trùng transferCode)</li>
     * </ol>
     *
     * @param request {@link CreateTopUpRequest} chứa coupleId, amount, bankId, bankName, note
     * @return {@link TopUpResponse} chứa thông tin yêu cầu và mã QR; hoặc thông báo lỗi
     */
    public TopUpResponse createTopUpRequest(CreateTopUpRequest request) {
        if (request.coupleId() == null || request.coupleId().isBlank()) {
            return TopUpResponse.failure("Couple ID is required");
        }
        if (request.amount() == null || request.amount() <= 0) {
            return TopUpResponse.failure("Amount must be positive");
        }
        if (coupleInfoRepository.findById(request.coupleId()).isEmpty()) {
            return TopUpResponse.failure("Couple not found");
        }

        for (int attempt = 0; attempt < MAX_TRANSFER_CODE_ATTEMPTS; attempt++) {
            String transferCode = generateTransferCode();
            String transferContent = buildTransferContent(transferCode, request.note());
            String qrUrl = buildQrUrl(request.amount(), transferContent);
            TopUpRequest topUpRequest = new TopUpRequest(
                    request.coupleId(),
                    request.amount(),
                    transferCode,
                    hasText(request.bankId()) ? request.bankId() : sePayProperties.bankCode(),
                    hasText(request.bankName()) ? request.bankName() : sePayProperties.bankName(),
                    sePayProperties.accountNumber(),
                    sePayProperties.accountName(),
                    transferContent,
                    transferContent,
                    qrUrl
            );

            try {
                TopUpRequest saved = topUpRequestRepository.save(topUpRequest);
                return toResponse(saved);
            } catch (DuplicateKeyException ignored) {
                // Retry with a new payment code if Mongo reports a rare collision.
            }
        }

        return TopUpResponse.failure("Could not generate a unique transfer code");
    }

    /**
     * Xử lý webhook từ SePay khi có giao dịch chuyển khoản đến.
     *
     * <p><b>Luồng xử lý chi tiết:</b></p>
     * <ol>
     *   <li>Validate webhook có {@code id}</li>
     *   <li>Kiểm tra deduplication: nếu đã xử lý theo sepayId hoặc referenceCode → trả về success</li>
     *   <li>Chỉ chấp nhận {@code transferType = "in"} (chuyển tiền đến)</li>
     *   <li>Validate số tiền &gt; 0</li>
     *   <li>Log warning nếu số tài khoản nhận không khớp (không reject vì BIDV/VA có thể khác format)</li>
     *   <li>Tìm yêu cầu nạp tiền phù hợp qua transferCode trong code/content/description</li>
     *   <li>Verify số tiền khớp với yêu cầu gốc</li>
     *   <li>Verify mã chuyển khoản khớp</li>
     *   <li>Atomic claim: PENDING → PROCESSING (dùng {@code findAndModify})</li>
     *   <li>Gọi {@code TransactionService.saveTransaction} để ghi giao dịch INCOME</li>
     *   <li>Nếu giao dịch thất bại → release claim về PENDING</li>
     *   <li>Nếu thành công → đánh dấu PAID</li>
     * </ol>
     *
     * @param webhook {@link SePayWebhookRequest} từ SePay
     * @return {@link TopUpWebhookResult} kết quả xử lý
     */
    public TopUpWebhookResult handleSePayWebhook(SePayWebhookRequest webhook) {
        if (webhook == null || webhook.id() == null) {
            return TopUpWebhookResult.failure("Missing SePay transaction id");
        }

        Optional<TopUpRequest> processedBySepayId = topUpRequestRepository.findBySepayId(webhook.id());
        if (processedBySepayId.filter(this::isPaid).isPresent()) {
            return TopUpWebhookResult.success("Duplicate webhook already processed");
        }
        if (hasText(webhook.referenceCode())) {
            Optional<TopUpRequest> processedByReference = topUpRequestRepository.findByReferenceCode(webhook.referenceCode());
            if (processedByReference.filter(this::isPaid).isPresent()) {
                return TopUpWebhookResult.success("Duplicate webhook already processed");
            }
        }

        if (!"in".equalsIgnoreCase(nullToEmpty(webhook.transferType()))) {
            return TopUpWebhookResult.failure("Only incoming SePay transfers can top up the wallet");
        }
        if (webhook.transferAmount() == null || webhook.transferAmount() <= 0) {
            return TopUpWebhookResult.failure("Transfer amount must be positive");
        }
        if (hasText(sePayProperties.accountNumber())
                && !matchesConfiguredReceivingAccount(webhook, sePayProperties.accountNumber())) {
            // Some banks/VA formats do not consistently populate account fields in webhook payloads.
            // Keep processing by transfer code + amount instead of hard rejecting valid top-ups.
            log.warn(
                    "SePay account mismatch, continue matching by transfer code: configuredAccount={}, webhookAccount={}, webhookSubAccount={}, webhookCode={}",
                    sePayProperties.accountNumber(),
                    webhook.accountNumber(),
                    webhook.subAccount(),
                    webhook.code()
            );
        }

        Optional<TopUpRequest> matchedRequest = findMatchingRequest(webhook);
        if (matchedRequest.isEmpty()) {
            return TopUpWebhookResult.failure("No pending top-up request matched this webhook");
        }

        TopUpRequest pendingRequest = matchedRequest.get();
        if (!pendingRequest.getAmount().equals(webhook.transferAmount())) {
            return TopUpWebhookResult.failure("Webhook amount does not match top-up request amount");
        }
        if (!matchesTransferCode(webhook, pendingRequest.getTransferCode())) {
            return TopUpWebhookResult.failure("Webhook code/content does not match top-up request");
        }

        TopUpRequest claimedRequest = claimPendingRequest(pendingRequest, webhook);
        if (claimedRequest == null) {
            Optional<TopUpRequest> alreadyPaid = topUpRequestRepository.findByTransferCode(pendingRequest.getTransferCode())
                    .filter(this::isPaid);
            if (alreadyPaid.isPresent()) {
                return TopUpWebhookResult.success("Duplicate webhook already processed");
            }
            return TopUpWebhookResult.failure("Top-up request is not pending");
        }

        TransactionResponse transactionResponse = transactionService.saveTransaction(
                claimedRequest.getCoupleId(),
                claimedRequest.getAmount(),
                TransactionType.INCOME,
                "INCOME",
                "SePay top-up " + claimedRequest.getTransferCode()
        );

        if (!transactionResponse.success()) {
            releaseClaim(claimedRequest, transactionResponse.message());
            return TopUpWebhookResult.failure(transactionResponse.message());
        }

        markPaid(claimedRequest, transactionResponse.transactionId());
        return TopUpWebhookResult.success("Top-up processed");
    }

    private TopUpResponse toResponse(TopUpRequest request) {
        Long currentBalance = coupleInfoRepository.findById(request.getCoupleId())
                .map(couple -> couple.getTotalBalance())
                .orElse(null);
        return new TopUpResponse(
                true,
                "Top-up request created",
                request.getId(),
                request.getCoupleId(),
                request.getAmount(),
                request.getStatus().name(),
                request.getTransferCode(),
                request.getBankCode(),
                request.getBankName(),
                request.getAccountNumber(),
                request.getAccountName(),
                request.getTransferContent(),
                request.getQrContent(),
                request.getQrUrl(),
                currentBalance,
                request.getCreatedAt(),
                request.getPaidAt()
        );
    }

    /**
     * Tìm và trả về thông tin yêu cầu nạp tiền theo ID.
     *
     * @param id ID yêu cầu nạp tiền
     * @return {@link TopUpResponse} nếu tìm thấy
     */
    public Optional<TopUpResponse> findTopUpRequest(String id) {
        return topUpRequestRepository.findById(id).map(this::toResponse);
    }

    /**
     * Sinh mã chuyển khoản duy nhất: prefix (mặc định "YMW") + 10 ký tự UUID uppercase.
     */
    private String generateTransferCode() {
        String prefix = hasText(sePayProperties.transferPrefix()) ? sePayProperties.transferPrefix() : "YMW";
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
        return prefix.toUpperCase(Locale.ROOT) + suffix;
    }

    /**
     * Xây dựng nội dung chuyển khoản: "transferCode note" hoặc "transferCode NAP VI CHUNG".
     */
    private String buildTransferContent(String transferCode, String note) {
        String suffix = hasText(note) ? " " + note.trim() : " NAP VI CHUNG";
        return (transferCode + suffix).trim();
    }

    /**
     * Xây dựng URL mã QR từ SePay với thông tin tài khoản, số tiền và nội dung.
     */
    private String buildQrUrl(Long amount, String transferContent) {
        String query = "acc=" + encode(sePayProperties.accountNumber())
                + "&bank=" + encode(sePayProperties.bankCode())
                + "&amount=" + amount
                + "&des=" + encode(transferContent)
                + "&template=" + encode(sePayProperties.qrTemplate());
        return "https://qr.sepay.vn/img?" + query;
    }

    private String encode(String value) {
        return URLEncoder.encode(nullToEmpty(value), StandardCharsets.UTF_8);
    }

    /**
     * Tìm yêu cầu nạp tiền phù hợp với webhook.
     * Ưu tiên match theo code, sau đó tìm trong content/description.
     */
    private Optional<TopUpRequest> findMatchingRequest(SePayWebhookRequest webhook) {
        if (hasText(webhook.code())) {
            Optional<TopUpRequest> byCode = topUpRequestRepository.findByTransferCode(webhook.code().trim());
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
        return topUpRequestRepository.findAll().stream()
                .filter(request -> request.getStatus() == TopUpRequestStatus.PENDING
                        || request.getStatus() == TopUpRequestStatus.PAID)
                .filter(request -> request.getTransferCode() != null
                        && request.getTransferCode().toUpperCase(Locale.ROOT).startsWith(prefix))
                .filter(request -> searchableText.contains(request.getTransferCode().toUpperCase(Locale.ROOT)))
                .findFirst();
    }

    /**
     * Kiểm tra mã chuyển khoản khớp: match trực tiếp qua code hoặc tìm trong content/description.
     */
    private boolean matchesTransferCode(SePayWebhookRequest webhook, String transferCode) {
        if (!hasText(transferCode)) {
            return false;
        }
        if (hasText(webhook.code()) && transferCode.equalsIgnoreCase(webhook.code().trim())) {
            return true;
        }
        String upperCode = transferCode.toUpperCase(Locale.ROOT);
        String searchableText = (nullToEmpty(webhook.content()) + " " + nullToEmpty(webhook.description()))
                .toUpperCase(Locale.ROOT);
        return searchableText.contains(upperCode);
    }

    /**
     * BIDV/VA webhooks may carry the VA in subAccount while accountNumber is the parent account.
     * Accept either field when matching against configured receiving account.
     */
    private boolean matchesConfiguredReceivingAccount(SePayWebhookRequest webhook, String configuredAccountNumber) {
        String configured = nullToEmpty(configuredAccountNumber).trim();
        if (configured.isBlank()) {
            return true;
        }
        String accountNumber = nullToEmpty(webhook.accountNumber()).trim();
        String subAccount = nullToEmpty(webhook.subAccount()).trim();
        return configured.equalsIgnoreCase(accountNumber) || configured.equalsIgnoreCase(subAccount);
    }

    /**
     * Atomic claim: chuyển trạng thái PENDING → PROCESSING chỉ khi request vẫn đang PENDING
     * và số tiền khớp. Sử dụng findAndModify để đảm bảo chỉ một webhook xử lý được.
     *
     * @return request đã được claim (PROCESSING), hoặc null nếu không claim được
     */
    private TopUpRequest claimPendingRequest(TopUpRequest request, SePayWebhookRequest webhook) {
        Query query = new Query(Criteria.where("id").is(request.getId())
                .and("status").is(TopUpRequestStatus.PENDING)
                .and("amount").is(webhook.transferAmount()));
        Update update = new Update()
                .set("status", TopUpRequestStatus.PROCESSING)
                .set("sepayId", webhook.id())
                .set("referenceCode", emptyToNull(webhook.referenceCode()))
                .set("webhookContent", webhook.content())
                .set("webhookDescription", webhook.description())
                .set("updatedAt", Instant.now())
                .unset("lastError");

        return mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), TopUpRequest.class);
    }

    /**
     * Release claim: hoàn tác PROCESSING → PENDING khi ghi giao dịch thất bại.
     * Xóa sepayId/referenceCode để webhook khác có thể retry.
     */
    private void releaseClaim(TopUpRequest request, String error) {
        Query query = new Query(Criteria.where("id").is(request.getId())
                .and("status").is(TopUpRequestStatus.PROCESSING));
        Update update = new Update()
                .set("status", TopUpRequestStatus.PENDING)
                .set("lastError", error)
                .set("updatedAt", Instant.now())
                .unset("sepayId")
                .unset("referenceCode");
        mongoTemplate.updateFirst(query, update, TopUpRequest.class);
    }

    /**
     * Đánh dấu yêu cầu nạp tiền đã thanh toán: PROCESSING → PAID.
     */
    private void markPaid(TopUpRequest request, String transactionId) {
        Query query = new Query(Criteria.where("id").is(request.getId())
                .and("status").is(TopUpRequestStatus.PROCESSING));
        Update update = new Update()
                .set("status", TopUpRequestStatus.PAID)
                .set("transactionId", transactionId)
                .set("paidAt", Instant.now())
                .set("updatedAt", Instant.now());
        mongoTemplate.updateFirst(query, update, TopUpRequest.class);
    }

    private boolean isPaid(TopUpRequest request) {
        return request.getStatus() == TopUpRequestStatus.PAID;
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
