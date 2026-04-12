package com.mobileproject.mobileprojectbackend.transaction;

import com.mobileproject.mobileprojectbackend.auth.CoupleInfo;
import com.mobileproject.mobileprojectbackend.auth.CoupleInfoRepository;
import com.mobileproject.mobileprojectbackend.transaction.dto.TransactionResponse;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CoupleInfoRepository coupleInfoRepository;
    private final MongoTemplate mongoTemplate;

    public TransactionService(TransactionRepository transactionRepository,
                              CoupleInfoRepository coupleInfoRepository,
                              MongoTemplate mongoTemplate) {
        this.transactionRepository = transactionRepository;
        this.coupleInfoRepository = coupleInfoRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public TransactionResponse saveTransaction(String coupleId, Long amount, TransactionType type,
                                                String category, String note) {
        if (coupleId == null || coupleId.isBlank()) {
            return TransactionResponse.failure("Couple ID is required");
        }
        if (amount == null || amount <= 0) {
            return TransactionResponse.failure("Amount must be positive");
        }
        if (type == null) {
            return TransactionResponse.failure("Transaction type is required");
        }

        CoupleInfo coupleInfo = coupleInfoRepository.findById(coupleId).orElse(null);
        if (coupleInfo == null) {
            return TransactionResponse.failure("Couple not found");
        }

        Transaction transaction = new Transaction(coupleId, amount, type, category, note);
        Transaction savedTransaction = transactionRepository.save(transaction);

        long balanceChange = type == TransactionType.INCOME ? amount : -amount;

        Query query = new Query(Criteria.where("id").is(coupleId));
        Update update = new Update().inc("totalBalance", balanceChange);
        mongoTemplate.updateFirst(query, update, CoupleInfo.class);

        CoupleInfo updatedCouple = coupleInfoRepository.findById(coupleId).orElse(coupleInfo);

        return TransactionResponse.success(
                savedTransaction.getId(),
                amount,
                type.name(),
                category,
                note,
                updatedCouple.getTotalBalance()
        );
    }
}