package com.kieronquinn.app.smartspacer.plugin.notifications.providers

import com.kieronquinn.app.smartspacer.plugin.notifications.complications.WhatsAppLegacyComplication

class WhatsAppBadgeReceiverProvider: BaseBadgeReceiverProvider() {

    override val packageName = WhatsAppLegacyComplication.PACKAGE_NAME

}