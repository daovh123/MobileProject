package com.mobileproject.mobileprojectbackend.payout;

import com.mobileproject.mobileprojectbackend.payout.dto.CreatePayoutRequest;
import com.mobileproject.mobileprojectbackend.payout.dto.PayoutResponse;
import com.mobileproject.mobileprojectbackend.topup.SePayProperties;
import com.mobileproject.mobileprojectbackend.topup.dto.SePayWebhookRequest;
import com.mobileproject.mobileprojectbackend.topup.dto.SePayWebhookResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller quản lý yêu cầu rút tiền (payout) từ ví chung qua SePay.
 *
 * <p>Base path: {@code /api/v1/payouts}</p>
 */
@RestController
@RequestMapping("/api/v1/payouts")
public class PayoutController {

    private static final Logger log = LoggerFactory.getLogger(PayoutController.class);

    private final PayoutService payoutService;
    private final SePayProperties sePayProperties;

    public PayoutController(PayoutService payoutService, SePayProperties sePayProperties) {
        this.payoutService = payoutService;
        this.sePayProperties = sePayProperties;
    }

    /**
     * Tạo yêu cầu rút tiền mới.
     *
     * <p><b>POST</b> {@code /api/v1/payouts}</p>
     *
     * @param request {@link CreatePayoutRequest} với validation Jakarta Bean Validation
     * @return {@link PayoutResponse} với thông tin yêu cầu; 400 nếu số dư không đủ hoặc lỗi
     */
    @PostMapping
    public ResponseEntity<PayoutResponse> createPayout(@Valid @RequestBody CreatePayoutRequest request) {
        PayoutResponse response = payoutService.createPayoutRequest(request);
        if (response.success()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Lấy thông tin yêu cầu rút tiền theo ID.
     *
     * <p><b>GET</b> {@code /api/v1/payouts/{id}}</p>
     *
     * @param id ID yêu cầu rút tiền (path variable)
     * @return {@link PayoutResponse} nếu tìm thấy; 404 nếu không tồn tại
     */
    @GetMapping("/{id}")
    public ResponseEntity<PayoutResponse> getPayout(@PathVariable String id) {
        return payoutService.findPayoutRequest(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Webhook endpoint nhận callback từ SePay khi giao dịch rút tiền được xác nhận.
     *
     * <p><b>POST</b> {@code /api/v1/payouts/sepay/webhook}</p>
     *
     * <p><b>Authentication:</b> Kiểm tra header {@code Authorization: Apikey <key>}
     * khớp với cấu hình {@code sepay.api-key}. Nếu không cấu hình → cho phép tất cả.</p>
     *
     * @param authorization header Authorization (optional)
     * @param request       {@link SePayWebhookRequest} payload từ SePay
     * @return {@link SePayWebhookResponse} với {@code success: true/false};
     *         401 nếu unauthorized; 400 nếu webhook bị reject
     */
    @PostMapping("/sepay/webhook")
    public ResponseEntity<SePayWebhookResponse> handleWebhook(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody SePayWebhookRequest request) {
        if (!isAuthorized(authorization)) {
            log.warn("SePay payout webhook unauthorized: auth header mismatch");
            return ResponseEntity.status(401).body(new SePayWebhookResponse(false));
        }

        PayoutWebhookResult result = payoutService.handleSePayWebhook(request);
        if (result.success()) {
            log.info("SePay payout webhook processed: id={}, amount={}, transferType={}, code={}",
                    request.id(), request.transferAmount(), request.transferType(), request.code());
            return ResponseEntity.ok(new SePayWebhookResponse(true));
        }
        log.warn("SePay payout webhook rejected: reason={}, id={}, amount={}, transferType={}, code={}",
                result.message(), request.id(), request.transferAmount(), request.transferType(), request.code());
        return ResponseEntity.badRequest().body(new SePayWebhookResponse(false));
    }

    private boolean isAuthorized(String authorization) {
        String configuredApiKey = sePayProperties.apiKey();
        if (configuredApiKey == null || configuredApiKey.isBlank()) {
            return true;
        }
        return ("Apikey " + configuredApiKey).equals(authorization);
    }
}

