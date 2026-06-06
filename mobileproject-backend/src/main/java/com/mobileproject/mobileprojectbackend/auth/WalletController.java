package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.WalletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST quản lý ví chung của cặp đôi.
 *
 * <p>Base path: {@code /api/v1/wallet}</p>
 * <p>Endpoint này công khai (permitAll trong SecurityConfig).</p>
 */
@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final CoupleInfoRepository coupleInfoRepository;

    public WalletController(CoupleInfoRepository coupleInfoRepository) {
        this.coupleInfoRepository = coupleInfoRepository;
    }

    /**
     * Lấy thông tin ví chung của cặp đôi theo coupleId.
     *
     * <ul>
     *   <li>HTTP Method: {@code GET}</li>
     *   <li>Path: {@code /api/v1/wallet/{coupleId}}</li>
     *   <li>Auth: Không yêu cầu</li>
     *   <li>Path variable: {@code coupleId} - ID của cặp đôi</li>
     *   <li>Response: {@link WalletResponse} (idCouple, walletName, totalBalance)</li>
     *   <li>HTTP 200: Thành công</li>
     *   <li>HTTP 400: CoupleId không tồn tại</li>
     * </ul>
     *
     * @param coupleId ID của cặp đôi
     * @return ResponseEntity chứa WalletResponse
     */
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