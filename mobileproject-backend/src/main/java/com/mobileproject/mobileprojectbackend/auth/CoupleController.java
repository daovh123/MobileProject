package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.CoupleCodeResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleRequestActionResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleRequestCreateRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleRequestDecisionRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.CouplePartnerProfileResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleStatusResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/couple")
public class CoupleController {

    private final CoupleService coupleService;

    public CoupleController(CoupleService coupleService) {
        this.coupleService = coupleService;
    }

    @PostMapping("/code")
    public ResponseEntity<CoupleCodeResponse> generateMyCode(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(coupleService.generateMyCode(authorizationHeader));
    }

    @GetMapping("/status")
    public ResponseEntity<CoupleStatusResponse> getStatus(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(coupleService.getStatus(authorizationHeader));
    }

    @GetMapping("/partner-profile")
    public ResponseEntity<CouplePartnerProfileResponse> getPartnerProfile(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(coupleService.getPartnerProfile(authorizationHeader));
    }

    @PostMapping("/requests")
    public ResponseEntity<CoupleRequestActionResponse> createRequest(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @Valid @RequestBody CoupleRequestCreateRequest request) {
        CoupleRequestActionResponse response = coupleService.createRequest(authorizationHeader, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/requests/{requestId}/decision")
    public ResponseEntity<CoupleRequestActionResponse> decideRequest(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable String requestId,
            @RequestBody CoupleRequestDecisionRequest request) {
        return ResponseEntity.ok(coupleService.decideRequest(authorizationHeader, requestId, request));
    }
}
