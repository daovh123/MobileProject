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

data class ResolvedQrTransfer(
    val accountNumber: String? = null,
    val bank: VietnamBank? = null,
    val amount: Long? = null,
    val note: String? = null,
    val rawContent: String,
    val warning: String? = null,
)

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
            warning = "QR nay chi chua link anh, chua tach duoc thong tin chuyen khoan.",
        )
    }

    val parsed = parseVietQrPayload(trimmed)
    if (parsed != null) {
        return parsed.copy(rawContent = trimmed)
    }

    return ResolvedQrTransfer(
        rawContent = trimmed,
        note = trimmed.take(160),
        warning = "Khong nhan dien duoc VietQR de tu dong dien thong tin.",
    )
}

private fun looksLikeImageUrl(value: String): Boolean {
    val lower = value.lowercase()
    return (lower.startsWith("http://") || lower.startsWith("https://")) &&
        (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".webp"))
}

private suspend fun decodeQrFromRemoteImage(url: String): String? {
    val bitmap = withContext(Dispatchers.IO) {
        runCatching {
            URL(url).openStream().use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
    } ?: return null
    return decodeQrFromBitmap(bitmap)
}

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

private fun parseVietQrPayload(raw: String): ResolvedQrTransfer? {
    if (!raw.startsWith("000201")) return null

    val root = parseTlv(raw)
    val merchant = root["38"] ?: root["26"] ?: return null
    val merchantFields = parseTlv(merchant)
    val bankBin = merchantFields["01"]
    val accountNumber = merchantFields["02"]?.trim()
    if (bankBin.isNullOrBlank() || accountNumber.isNullOrBlank()) return null

    val bankShortName = bankBinToShortName[bankBin]
    val bank = VietnamBankCatalog.banks.firstOrNull {
        it.shortName.equals(bankShortName, ignoreCase = true) ||
            it.name.equals(bankShortName, ignoreCase = true)
    }

    val amount = root["54"]?.replace(",", "")?.toLongOrNull()
    val additional = root["62"]?.let(::parseTlv)
    val note = additional?.get("08")
        ?: additional?.get("07")
        ?: additional?.get("05")

    return ResolvedQrTransfer(
        accountNumber = accountNumber,
        bank = bank,
        amount = amount,
        note = note,
        rawContent = raw,
        warning = if (bank == null) "Da doc QR nhung chua map duoc ngan hang tu ma $bankBin." else null,
    )
}

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

