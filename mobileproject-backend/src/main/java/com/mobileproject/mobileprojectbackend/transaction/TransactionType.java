package com.mobileproject.mobileprojectbackend.transaction;

/**
 * Loại giao dịch tài chính.
 *
 * <ul>
 *   <li>{@link #INCOME} – Thu nhập / Nạp tiền vào ví</li>
 *   <li>{@link #EXPENSE} – Chi tiêu / Rút tiền khỏi ví</li>
 * </ul>
 */
public enum TransactionType {
    INCOME,
    EXPENSE
}