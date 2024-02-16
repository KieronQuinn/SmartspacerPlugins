package com.kieronquinn.app.smartspacer.plugin.amazon

const val AMZN_APP_CTXT = "amzn-app-ctxt"

/**
 *  Package names from https://amazon.com/.well-known/assetlinks.json, in reverse order of
 *  likeliness of being installed
 */
val PACKAGE_NAMES = arrayOf(
    "com.amazon.mShop.android.beta",
    "cn.amazon.mShop.android",
    "in.amazon.mShop.android.business.shopping",
    "in.amazon.mShop.android.shopping",
    "com.amazon.mShop.android.business.shopping",
    "com.amazon.mShop.android.shopping"
)

fun <T> getFromPackageName(block: (packageName: String) -> T?): T? {
    return PACKAGE_NAMES.firstNotNullOfOrNull(block)
}

const val DEFAULT_APP_VERSION = "28.3.0.100"

val HEADERS = mapOf(
    "X-Requested-With" to "com.amazon.mShop.android.shopping"
)

val ALLOWED_COOKIE_HOSTS = listOf(
    "amazon.ae",
    "amazon.ca",
    "amazon.cn",
    "amazon.com",
    "amazon.de",
    "amazon.eg",
    "amazon.es",
    "amazon.eu",
    "amazon.fr",
    "amazon.in",
    "amazon.it",
    "amazon.nl",
    "amazon.pl",
    "amazon.sa",
    "amazon.se",
    "amazon.sg",
    "amazon.co.jp",
    "amazon.co.uk",
    "amazon.com.au",
    "amazon.com.br",
    "amazon.com.mx",
    "amazon.com.sg",
    "amazon.com.tr",
    "amazon.com.co",
    "amazon.cl",
    "amazon.com.ng",
    "amazon.com.be",
    "amazon.co.za",
    "souq.com",
    "primevideo.com",
    "amazon-adsystem.com"
)