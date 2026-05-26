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

    @PostMapping
    public ResponseEntity<TopUpResponse> createTopUpRequest(@Valid @RequestBody CreateTopUpRequest request) {
        TopUpResponse response = topUpService.createTopUpRequest(request);
        if (response.success()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TopUpResponse> getTopUpRequest(@PathVariable String id) {
        return topUpService.findTopUpRequest(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

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
