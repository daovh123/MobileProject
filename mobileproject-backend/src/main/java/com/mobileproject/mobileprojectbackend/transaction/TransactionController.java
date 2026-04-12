package com.mobileproject.mobileprojectbackend.transaction;

import com.mobileproject.mobileprojectbackend.transaction.dto.IncomeRequest;
import com.mobileproject.mobileprojectbackend.transaction.dto.TransactionRequest;
import com.mobileproject.mobileprojectbackend.transaction.dto.TransactionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionService transactionService, TransactionRepository transactionRepository) {
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
    }

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

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionRequest request) {
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

    @PostMapping("/income")
    public ResponseEntity<TransactionResponse> processIncome(@RequestBody IncomeRequest request) {
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