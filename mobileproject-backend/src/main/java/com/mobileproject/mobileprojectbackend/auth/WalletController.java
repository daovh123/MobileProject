package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.WalletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final CoupleInfoRepository coupleInfoRepository;

    public WalletController(CoupleInfoRepository coupleInfoRepository) {
        this.coupleInfoRepository = coupleInfoRepository;
    }

    @GetMapping("/{coupleId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable String coupleId) {
        CoupleInfo coupleInfo = coupleInfoRepository.findById(coupleId)
                .orElseThrow(() -> new IllegalArgumentException("Couple not found with id: " + coupleId));

        WalletResponse walletResponse = new WalletResponse(
                coupleInfo.getIdCouple(),
                coupleInfo.getTotalBalance()
        );

        return ResponseEntity.ok(walletResponse);
    }
}