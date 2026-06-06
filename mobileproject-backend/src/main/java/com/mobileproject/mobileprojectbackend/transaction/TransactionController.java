package com.mobileproject.mobileprojectbackend.transaction;

import com.mobileproject.mobileprojectbackend.transaction.dto.IncomeRequest;
import com.mobileproject.mobileprojectbackend.transaction.dto.TransactionRequest;
import com.mobileproject.mobileprojectbackend.transaction.dto.TransactionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller quản lý giao dịch tài chính của cặp đôi.
 *
 * <p>Base path: {@code /api/v1/transactions}</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionService transactionService, TransactionRepository transactionRepository) {
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Lấy tất cả giao dịch trong hệ thống (debug only).
     *
     * <p><b>GET</b> {@code /api/v1/transactions/debug-all}</p>
     *
     * @return danh sách tất cả giao dịch
     */
    @GetMapping("/debug-all")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> all = transactionRepository.findAll();
        System.out.println("=== DEBUG: All transactions in DB ===");
        System.out.println("Total: " + all.size());
        for (Transaction t : all) {
            System.out.println("Transaction: id=" + t.getId() + ", coupleId=" + t.getCoupleId() + 
                ", type=" + t.getType() + ", amount=" + t.getAmount() + 
                ", category=" + t.getCategory() + ", createdAt=" + t.getCreatedAt());
        }
        return ResponseEntity.ok(all);
    }

    /**
     * Lấy danh sách giao dịch của một cặp đôi, sắp xếp mới nhất trước.
     *
     * <p><b>GET</b> {@code /api/v1/transactions?coupleId=...}</p>
     *
     * @param coupleId ID của cặp đôi (query param)
     * @return danh sách giao dịch theo thứ tự thời gian giảm dần
     */
    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactions(@RequestParam String coupleId) {
        List<Transaction> transactions = transactionRepository.findByCoupleIdOrderByCreatedAtDesc(coupleId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Tạo giao dịch thu/chi mới.
     *
     * <p><b>POST</b> {@code /api/v1/transactions}</p>
     *
     * @param request {@link TransactionRequest} chứa coupleId, amount, type, category, note
     * @return {@link TransactionResponse} với thông tin giao dịch và số dư cập nhật; 400 nếu lỗi
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionRequest request) {
        log.info("Payload nhận được: {}", request);
        TransactionResponse response = transactionService.saveTransaction(
                request.coupleId(),
                request.amount(),
                request.type(),
                request.category(),
                request.note()
        );

        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Xử lý nạp tiền vào ví chung hoặc mục tiêu tiết kiệm.
     *
     * <p><b>POST</b> {@code /api/v1/transactions/income}</p>
     *
     * @param request {@link IncomeRequest} chứa coupleId, amount, targetType (WALLET/GOAL), goalId, note
     * @return {@link TransactionResponse} với kết quả xử lý; 400 nếu lỗi
     */
    @PostMapping("/income")
    public ResponseEntity<TransactionResponse> processIncome(@RequestBody IncomeRequest request) {
        log.info("Payload nhận được: {}", request);
        TransactionResponse response = transactionService.processIncome(
                request.coupleId(),
                request.amount(),
                request.targetType(),
                request.goalId(),
                request.note()
        );

        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}