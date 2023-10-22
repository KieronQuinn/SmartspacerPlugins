package com.kieronquinn.app.smartspacer.plugin.amazon.model

import androidx.annotation.StringRes
import com.kieronquinn.app.smartspacer.plugin.amazon.R

enum class AmazonDomain(
    val domainName: String?,
    @StringRes
    val nameRes: Int,
    val trackingDomain: TrackingDomain? = null
) {
    AE("www.amazon.ae", R.string.domain_AE),
    CA("www.amazon.ca", R.string.domain_CA, TrackingDomain.NA),
    CN("www.amazon.cn", R.string.domain_CN),
    JP("www.amazon.co.jp", R.string.domain_JP),
    UK("www.amazon.co.uk", R.string.domain_UK, TrackingDomain.EU),
    US("www.amazon.com", R.string.domain_US, TrackingDomain.NA),
    AU("www.amazon.com.au", R.string.domain_AU),
    BE("www.amazon.com.be", R.string.domain_BE, TrackingDomain.EU),
    BR("www.amazon.com.br", R.string.domain_BR),
    MX("www.amazon.com.mx", R.string.domain_MX, TrackingDomain.NA),
    TR("www.amazon.com.tr", R.string.domain_TR),
    DE("www.amazon.de", R.string.domain_DE, TrackingDomain.EU),
    EG("www.amazon.eg", R.string.domain_EG),
    ES("www.amazon.es", R.string.domain_ES, TrackingDomain.EU),
    FR("www.amazon.fr", R.string.domain_FR, TrackingDomain.EU),
    IN("www.amazon.in", R.string.domain_IN),
    NL("www.amazon.nl", R.string.domain_NL, TrackingDomain.EU),
    PL("www.amazon.pl", R.string.domain_PL, TrackingDomain.EU),
    SA("www.amazon.sa", R.string.domain_SA),
    SE("www.amazon.se", R.string.domain_SE, TrackingDomain.EU),
    SG("www.amazon.sg", R.string.domain_SG),
    UNKNOWN(null, R.string.domain_unknown)
}