/**
 * # QrTransferResolver - Giải mã nội dung QR chuyển khoản
 *
 * Module chịu trách nhiệm phân tích và giải mã raw string từ mã QR thành
 * thông tin chuyển khoản có cấu trúc [ResolvedQrTransfer].
 *
 * ## Xử lý theo thứ tự ưu tiên
 * 1. **URL ảnh**: nếu raw là link ảnh (.png/.jpg/.jpeg/.webp), tải ảnh về và decode QR bằng MLKit
 * 2. **VietQR EMVCo**: parse payload theo chuẩn EMVCo TLV (Tag-Length-Value):
 *    - Tag 38/26: merchant data chứa BIN ngân hàng (tag 01) và số tài khoản (tag 02)
 *    - Tag 54: số tiền
 *    - Tag 62: dữ liệu bổ sung, chứa nội dung chuyển khoản (tag 08/07/05)
 * 3. **Fallback**: trả về raw content làm note, kèm warning
 *
 * ## Data model
 * - [ResolvedQrTransfer]: kết quả giải mã bao gồm accountNumber, bank, amount, note, warning
 *
 * ## Key integrations
 * - **MLKit Barcode Scanning**: decode QR từ bitmap (ảnh remote)
 * - **VietnamBankCatalog**: ánh xạ BIN code → ngân hàng Việt Nam
 */
package com.example.mobileproject.presentation.ui.screen.wallet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.mobileproject.presentation.model.wallet.VietnamBank
import com.example.mobileproject.presentation.model.wallet.VietnamBankCatalog
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.coroutines.resume

/**
 * Kết quả giải mã QR chuyển khoản.
 *
 * @param accountNumber số tài khoản đích
 * @param bank đối tượng ngân hàng (null nếu không tìm thấy)
 * @param amount số tiền (null nếu QR không chứa)
 * @param note nội dung chuyển khoản
 * @param rawContent raw string gốc từ QR
 * @param warning cảnh báo (ví dụ: không tìm thấy ngân hàng theo BIN)
 */
data class ResolvedQrTransfer(
    val accountNumber: String? = null,
    val bank: VietnamBank? = null,
    val amount: Long? = null,
    val note: String? = null,
    val rawContent: String,
    val warning: String? = null,
)

/**
 * Ánh xạ BIN code (6 số đầu số thẻ) → tên viết tắt ngân hàng Việt Nam.
 * Dùng để xác định ngân hàng từ VietQR payload.
 */
private val bankBinToShortName = mapOf(
    "970418" to "BIDV",
    "970422" to "MB",
    "970436" to "VCB",
    "970415" to "VTB",
    "970407" to "TCB",
    "970432" to "VPB",
    "970416" to "ACB",
    "970423" to "TPB",
    "970403" to "STB",
    "970405" to "AGR",
    "970448" to "OCB",
    "970431" to "EIB",
    "970443" to "HDB",
    "970441" to "VIB",
    "970437" to "HLB",
    "970454" to "SEA",
    "970438" to "BVB",
    "970440" to "SHB",
)

/**
 * Entry point: giải mã raw QR string thành [ResolvedQrTransfer].
 * Xử lý theo thứ tự: URL ảnh → VietQR EMVCo → fallback.
 *
 * @param context Android context (cần cho MLKit image processing)
 * @param raw raw string từ mã QR
 * @return ResolvedQrTransfer hoặc null nếu input rỗng
 */
suspend fun resolveTransferQr(
    context: Context,
    raw: String,
): ResolvedQrTransfer? {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return null

    if (looksLikeImageUrl(trimmed)) {
        val nestedRaw = decodeQrFromRemoteImage(trimmed)
        if (!nestedRaw.isNullOrBlank() && nestedRaw != trimmed) {
            return resolveTransferQr(context, nestedRaw)
        }
        return ResolvedQrTransfer(
            rawContent = trimmed,
            warning = "QR này chỉ chứa link ảnh, chưa tách được thông tin chuyển khoản.",
        )
    }

    val parsed = parseVietQrPayload(trimmed)
    if (parsed != null) {
        return parsed.copy(rawContent = trimmed)
    }

    return ResolvedQrTransfer(
        rawContent = trimmed,
        note = trimmed.take(160),
        warning = "Không nhận diện được VietQR để tự động điền thông tin.",
    )
}

/**
 * Kiểm tra chuỗi có phải URL ảnh (.png, .jpg, .jpeg, .webp) không.
 */
private fun looksLikeImageUrl(value: String): Boolean {
    val lower = value.lowercase()
    return (lower.startsWith("http://") || lower.startsWith("https://")) &&
        (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".webp"))
}

/**
 * Tải ảnh từ URL remote và decode QR bằng MLKit.
 * Chạy trên Dispatchers.IO để không block main thread.
 */
private suspend fun decodeQrFromRemoteImage(url: String): String? {
    val bitmap = withContext(Dispatchers.IO) {
        runCatching {
            URL(url).openStream().use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
    } ?: return null
    return decodeQrFromBitmap(bitmap)
}

/**
 * Decode QR từ Bitmap bằng MLKit BarcodeScanning.
 * Sử dụng suspendCancellableCoroutine để bridge callback-based API sang coroutine.
 */
private suspend fun decodeQrFromBitmap(bitmap: Bitmap): String? {
    val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    )
    val image = InputImage.fromBitmap(bitmap, 0)
    return suspendCancellableCoroutine { continuation ->
        scanner.process(image)
            .addOnSuccessListener { codes ->
                continuation.resume(codes.firstOrNull()?.rawValue)
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }
}

/**
 * Parse VietQR EMVCo payload theo chuẩn TLV (Tag-Length-Value).
 * Chuẩn VietQR bắt đầu bằng "000201", chứa merchant data (tag 38 hoặc 26)
 * với BIN ngân hàng (tag 01) và số tài khoản (tag 02).
 *
 * @param raw raw QR string
 * @return ResolvedQrTransfer hoặc null nếu không phải VietQR
 */
private fun parseVietQrPayload(raw: String): ResolvedQrTransfer? {
    // VietQR EMVCo payload luôn bắt đầu bằng "000201" (Payload Format Indicator)
    if (!raw.startsWith("000201")) return null

    // Parse root TLV fields, tìm merchant data (tag 38 = merchant, tag 26 = sub-merchant)
    val root = parseTlv(raw)
    val merchant = root["38"] ?: root["26"] ?: return null
    val merchantFields = parseTlv(merchant)
    // Tag 01: AID/BIN ngân hàng, Tag 02: số tài khoản
    val bankBin = merchantFields["01"]
    val accountNumber = merchantFields["02"]?.trim()
    if (bankBin.isNullOrBlank() || accountNumber.isNullOrBlank()) return null

    // Ánh xạ BIN → ngân hàng qua VietnamBankCatalog
    val bankShortName = bankBinToShortName[bankBin]
    val bank = VietnamBankCatalog.banks.firstOrNull {
        it.shortName.equals(bankShortName, ignoreCase = true) ||
            it.name.equals(bankShortName, ignoreCase = true)
    }

    // Tag 54: số tiền, Tag 62: dữ liệu bổ sung (chứa nội dung chuyển khoản)
    val amount = root["54"]?.replace(",", "")?.toLongOrNull()
    val additional = root["62"]?.let(::parseTlv)
    // Nội dung chuyển khoản nằm ở tag 08, 07, hoặc 05 trong Additional Data
    val note = additional?.get("08")
        ?: additional?.get("07")
        ?: additional?.get("05")

    return ResolvedQrTransfer(
        accountNumber = accountNumber,
        bank = bank,
        amount = amount,
        note = note,
        rawContent = raw,
        warning = if (bank == null) "Đã đọc QR nhưng chưa tìm thấy ngân hàng tương ứng với mã BIN $bankBin." else null,
    )
}

/**
 * Parse chuỗi TLV (Tag-Length-Value) thành Map<tag, value>.
 * Mỗi field: 2 ký tự tag + 2 ký tự length + N ký tự value.
 */
private fun parseTlv(source: String): Map<String, String> {
    val result = linkedMapOf<String, String>()
    var index = 0
    while (index + 4 <= source.length) {
        val tag = source.substring(index, index + 2)
        val length = source.substring(index + 2, index + 4).toIntOrNull() ?: break
        val start = index + 4
        val end = start + length
        if (end > source.length) break
        result[tag] = source.substring(start, end)
        index = end
    }
    return result
}

