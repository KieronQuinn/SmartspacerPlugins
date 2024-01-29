package com.kieronquinn.app.smartspacer.plugin.amazon.model.api

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kieronquinn.app.smartspacer.plugin.amazon.R

enum class AmazonDomain(
    val domainName: String,
    @StringRes
    val nameRes: Int,
    @StringRes
    val countryRes: Int,
    @DrawableRes
    val iconRes: Int
) {
    AE("amazon.ae", R.string.domain_AE, R.string.domain_name_AE, R.drawable.flag_ae),
    CA("amazon.ca", R.string.domain_CA, R.string.domain_name_CA, R.drawable.flag_ca),
    CN("amazon.cn", R.string.domain_CN, R.string.domain_name_CN, R.drawable.flag_cn),
    US("amazon.com", R.string.domain_US, R.string.domain_name_US, R.drawable.flag_us),
    DE("amazon.de", R.string.domain_DE, R.string.domain_name_DE, R.drawable.flag_de),
    EG("amazon.eg", R.string.domain_EG, R.string.domain_name_EG, R.drawable.flag_eg),
    ES("amazon.es", R.string.domain_ES, R.string.domain_name_ES, R.drawable.flag_es),
    FR("amazon.fr", R.string.domain_FR, R.string.domain_name_FR, R.drawable.flag_fr),
    IN("amazon.in", R.string.domain_IN, R.string.domain_name_IN, R.drawable.flag_in),
    IT("amazon.it", R.string.domain_IT, R.string.domain_name_IT, R.drawable.flag_it),
    NL("amazon.nl", R.string.domain_NL, R.string.domain_name_NL, R.drawable.flag_nl),
    PL("amazon.pl", R.string.domain_PL, R.string.domain_name_PL, R.drawable.flag_pl),
    SA("amazon.sa", R.string.domain_SA, R.string.domain_name_SA, R.drawable.flag_sa),
    SE("amazon.se", R.string.domain_SE, R.string.domain_name_SE, R.drawable.flag_se),
    SG("amazon.sg", R.string.domain_SG, R.string.domain_name_SG, R.drawable.flag_sg),
    JP("amazon.co.jp", R.string.domain_JP, R.string.domain_name_JP, R.drawable.flag_jp),
    UK("amazon.co.uk", R.string.domain_UK, R.string.domain_name_UK, R.drawable.flag_uk),
    AU("amazon.com.au", R.string.domain_AU, R.string.domain_name_AU, R.drawable.flag_au),
    BR("amazon.com.br", R.string.domain_BR, R.string.domain_name_BR, R.drawable.flag_br),
    MX("amazon.com.mx", R.string.domain_MX, R.string.domain_name_MX, R.drawable.flag_mx),
    TR("amazon.com.tr", R.string.domain_TR, R.string.domain_name_TR, R.drawable.flag_tr),
    CO("amazon.com.co", R.string.domain_CO, R.string.domain_name_CO, R.drawable.flag_co),
    CL("amazon.cl", R.string.domain_CL, R.string.domain_name_CL, R.drawable.flag_cl),
    NG("amazon.com.ng", R.string.domain_NG, R.string.domain_name_NG, R.drawable.flag_ng),
    BE("amazon.com.be", R.string.domain_BE, R.string.domain_name_BE, R.drawable.flag_be),
    ZA("amazon.com.za", R.string.domain_ZA, R.string.domain_name_ZA, R.drawable.flag_za),
}