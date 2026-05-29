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

    @PostMapping
    public ResponseEntity<PayoutResponse> createPayout(@Valid @RequestBody CreatePayoutRequest request) {
        PayoutResponse response = payoutService.createPayoutRequest(request);
        if (response.success()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayoutResponse> getPayout(@PathVariable String id) {
        return payoutService.findPayoutRequest(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

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

