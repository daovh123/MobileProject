package com.mobileproject.mobileprojectbackend.transaction;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findByCoupleIdOrderByCreatedAtDesc(String coupleId);
}