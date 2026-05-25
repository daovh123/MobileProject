package com.example.mobileproject.presentation.model.wallet

import com.example.mobileproject.R

data class VietnamBank(
    val id: String,
    val name: String,
    val shortName: String,
    val logoRes: Int,
)

object VietnamBankCatalog {
    val banks: List<VietnamBank> = listOf(
        VietnamBank("abbank", "ABBank", "AB", R.drawable.bank_logo_abbank),
        VietnamBank("acb", "ACB", "ACB", R.drawable.bank_logo_acb),
        VietnamBank("agribank", "Agribank", "AGR", R.drawable.bank_logo_agribank),
        VietnamBank("anz_bank", "ANZ Bank", "ANZ", R.drawable.bank_logo_anz_bank),
        VietnamBank("bacabank", "Bac A Bank", "BAB", R.drawable.bank_logo_bacabank),
        VietnamBank("baovietbank", "BaoVietBank", "BVB", R.drawable.bank_logo_baovietbank),
        VietnamBank("bidv", "BIDV", "BIDV", R.drawable.bank_logo_bidv),
        VietnamBank("bvbank", "BVBank", "BVB", R.drawable.bank_logo_bvbank),
        VietnamBank("cbbank", "CBBank", "CBB", R.drawable.bank_logo_cbbank),
        VietnamBank("co_opbank", "Co-opBank", "COOP", R.drawable.bank_logo_co_opbank),
        VietnamBank("dongabank", "DongA Bank", "DAB", R.drawable.bank_logo_dongabank),
        VietnamBank("eximbank", "Eximbank", "EIB", R.drawable.bank_logo_eximbank),
        VietnamBank("gpbank", "GPBank", "GPB", R.drawable.bank_logo_gpbank),
        VietnamBank("hdbank", "HDBank", "HDB", R.drawable.bank_logo_hdbank),
        VietnamBank("hong_leong_bank", "Hong Leong Bank", "HLB", R.drawable.bank_logo_hong_leong_bank),
        VietnamBank("hsbc", "HSBC", "HSBC", R.drawable.bank_logo_hsbc),
        VietnamBank("indovina", "Indovina Bank", "IVB", R.drawable.bank_logo_indovina),
        VietnamBank("kienlongbank", "KienlongBank", "KLB", R.drawable.bank_logo_kienlongbank),
        VietnamBank("lpbank", "LPBank", "LPB", R.drawable.bank_logo_lpbank),
        VietnamBank("mbbank", "MB Bank", "MB", R.drawable.bank_logo_mbbank),
        VietnamBank("msb", "MSB", "MSB", R.drawable.bank_logo_msb),
        VietnamBank("namabank", "Nam A Bank", "NAB", R.drawable.bank_logo_namabank),
        VietnamBank("ncb", "NCB", "NCB", R.drawable.bank_logo_ncb),
        VietnamBank("ocb", "OCB", "OCB", R.drawable.bank_logo_ocb),
        VietnamBank("oceanbank", "OceanBank", "OCEAN", R.drawable.bank_logo_oceanbank),
        VietnamBank("public_bank", "Public Bank", "PBB", R.drawable.bank_logo_public_bank),
        VietnamBank("pvcombank", "PVcomBank", "PVCB", R.drawable.bank_logo_pvcombank),
        VietnamBank("sacombank", "Sacombank", "STB", R.drawable.bank_logo_sacombank),
        VietnamBank("saigonbank", "Saigonbank", "SGB", R.drawable.bank_logo_saigonbank),
        VietnamBank("scb", "SCB", "SCB", R.drawable.bank_logo_scb),
        VietnamBank("seabank", "SeABank", "SEA", R.drawable.bank_logo_seabank),
        VietnamBank("shb", "SHB", "SHB", R.drawable.bank_logo_shb),
        VietnamBank("shinhan_bank", "Shinhan Bank", "SHIN", R.drawable.bank_logo_shinhan_bank),
        VietnamBank("standard_chartered", "Standard Chartered", "SC", R.drawable.bank_logo_standard_chartered),
        VietnamBank("techcombank", "Techcombank", "TCB", R.drawable.bank_logo_techcombank),
        VietnamBank("tpbank", "TPBank", "TPB", R.drawable.bank_logo_tpbank),
        VietnamBank("uob", "UOB", "UOB", R.drawable.bank_logo_uob),
        VietnamBank("vdb", "VDB", "VDB", R.drawable.bank_logo_vdb),
        VietnamBank("vib", "VIB", "VIB", R.drawable.bank_logo_vib),
        VietnamBank("vietabank", "VietABank", "VAB", R.drawable.bank_logo_vietabank),
        VietnamBank("vietbank", "Vietbank", "VB", R.drawable.bank_logo_vietbank),
        VietnamBank("vietcombank", "Vietcombank", "VCB", R.drawable.bank_logo_vietcombank_1),
        VietnamBank("vietinbank", "VietinBank", "CTG", R.drawable.bank_logo_vietinbank),
        VietnamBank("vpbank", "VPBank", "VPB", R.drawable.bank_logo_vpbank),
        VietnamBank("woori_bank", "Woori Bank", "WORI", R.drawable.bank_logo_woori_bank),
    )

    fun findById(id: String): VietnamBank? = banks.firstOrNull { it.id == id }
}
