package com.example.mobileproject.domain.entity

/**
 * Domain entity đại diện cho ví tiền chung của cặp đôi.
 * Mỗi cặp đôi có một ví dùng để quản lý thu chi và thực hiện giao dịch.
 *
 * @property idCouple Định danh của cặp đôi sở hữu ví, liên kết với hệ thống couple
 * @property name Tên hiển thị của ví (ví dụ: "Ví chung")
 * @property balance Số dư hiện tại của ví (đơn vị: VNĐ), được cập nhật sau mỗi giao dịch
 */
data class Wallet(
    val idCouple: String,
    val name: String,
    val balance: Long
)
