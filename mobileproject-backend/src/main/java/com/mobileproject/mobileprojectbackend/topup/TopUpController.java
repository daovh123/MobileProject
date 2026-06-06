package com.mobileproject.mobileprojectbackend.topup;

import com.mobileproject.mobileprojectbackend.topup.dto.CreateTopUpRequest;
import com.mobileproject.mobileprojectbackend.topup.dto.SePayWebhookRequest;
import com.mobileproject.mobileprojectbackend.topup.dto.SePayWebhookResponse;
import com.mobileproject.mobileprojectbackend.topup.dto.TopUpResponse;
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
 * REST Controller quản lý nạp tiền vào ví chung qua SePay.
 *
 * <p>Base paths: {@code /api/v1/top-ups} hoặc {@code /api/v1/topups} (alias)</p>
 */
@RestController
@RequestMapping({"/api/v1/top-ups", "/api/v1/topups"})
public class TopUpController {

    private static final Logger log = LoggerFactory.getLogger(TopUpController.class);

    private final TopUpService topUpService;
    private final SePayProperties sePayProperties;

    public TopUpController(TopUpService topUpService, SePayProperties sePayProperties) {
        this.topUpService = topUpService;
        this.sePayProperties = sePayProperties;
    }

    /**
     * Tạo yêu cầu nạp tiền mới.
     *
     * <p><b>POST</b> {@code /api/v1/top-ups}</p>
     *
     * @param request {@link CreateTopUpRequest} với validation Jakarta Bean Validation
     * @return {@link TopUpResponse} chứa mã QR và thông tin chuyển khoản; 400 nếu lỗi
     */
    @PostMapping
    public ResponseEntity<TopUpResponse> createTopUpRequest(@Valid @RequestBody CreateTopUpRequest request) {
        TopUpResponse response = topUpService.createTopUpRequest(request);
        if (response.success()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Lấy thông tin yêu cầu nạp tiền theo ID.
     *
     * <p><b>GET</b> {@code /api/v1/top-ups/{id}}</p>
     *
     * @param id ID yêu cầu nạp tiền (path variable)
     * @return {@link TopUpResponse} nếu tìm thấy; 404 nếu không tồn tại
     */
    @GetMapping("/{id}")
    public ResponseEntity<TopUpResponse> getTopUpRequest(@PathVariable String id) {
        return topUpService.findTopUpRequest(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Webhook endpoint nhận callback từ SePay khi có giao dịch chuyển khoản.
     *
     * <p><b>POST</b> {@code /api/v1/top-ups/sepay/webhook}</p>
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
    public ResponseEntity<SePayWebhookResponse> handleSePayWebhook(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody SePayWebhookRequest request) {
        if (!isAuthorized(authorization)) {
            log.warn("SePay webhook unauthorized: auth header mismatch");
            return ResponseEntity.status(401).body(new SePayWebhookResponse(false));
        }

        TopUpWebhookResult result = topUpService.handleSePayWebhook(request);
        if (result.success()) {
            log.info(
                    "SePay webhook processed: id={}, amount={}, transferType={}, referenceCode={}, code={}",
                    request.id(),
                    request.transferAmount(),
                    request.transferType(),
                    request.referenceCode(),
                    request.code()
            );
            return ResponseEntity.ok(new SePayWebhookResponse(true));
        }
        log.warn(
                "SePay webhook rejected: reason={}, id={}, amount={}, transferType={}, accountNumber={}, referenceCode={}, code={}",
                result.message(),
                request.id(),
                request.transferAmount(),
                request.transferType(),
                request.accountNumber(),
                request.referenceCode(),
                request.code()
        );
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
