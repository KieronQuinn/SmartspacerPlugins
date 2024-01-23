package com.kieronquinn.app.smartspacer.plugin.amazon.model.api

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kieronquinn.app.smartspacer.plugin.amazon.R

enum class AmazonDomain(
    val domainName: String,
    @StringRes
    val nameRes: Int,
    @DrawableRes
    val iconRes: Int
) {
    AE("amazon.ae", R.string.domain_AE, R.drawable.flag_ae),
    CA("amazon.ca", R.string.domain_CA, R.drawable.flag_ca),
    CN("amazon.cn", R.string.domain_CN, R.drawable.flag_cn),
    JP("amazon.co.jp", R.string.domain_JP, R.drawable.flag_jp),
    UK("amazon.co.uk", R.string.domain_UK, R.drawable.flag_uk),
    US("amazon.com", R.string.domain_US, R.drawable.flag_us),
    AU("amazon.com.au", R.string.domain_AU, R.drawable.flag_au),
    BE("amazon.com.be", R.string.domain_BE, R.drawable.flag_be),
    BR("amazon.com.br", R.string.domain_BR, R.drawable.flag_br),
    MX("amazon.com.mx", R.string.domain_MX, R.drawable.flag_mx),
    TR("amazon.com.tr", R.string.domain_TR, R.drawable.flag_tr),
    DE("amazon.de", R.string.domain_DE, R.drawable.flag_de),
    EG("amazon.eg", R.string.domain_EG, R.drawable.flag_eg),
    ES("amazon.es", R.string.domain_ES, R.drawable.flag_es),
    FR("amazon.fr", R.string.domain_FR, R.drawable.flag_fr),
    IN("amazon.in", R.string.domain_IN, R.drawable.flag_in),
    NL("amazon.nl", R.string.domain_NL, R.drawable.flag_nl),
    PL("amazon.pl", R.string.domain_PL, R.drawable.flag_pl),
    SA("amazon.sa", R.string.domain_SA, R.drawable.flag_sa),
    SE("amazon.se", R.string.domain_SE, R.drawable.flag_se),
    SG("amazon.sg", R.string.domain_SG, R.drawable.flag_sg)
}