package com.kieronquinn.app.smartspacer.plugin.googlewallet.extraproviders

import com.google.protobuf.MessageLite
import com.kieronquinn.app.smartspacer.plugin.googlewallet.extraproviders.transitcard.TrainlineProvider
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.extras.TransitCardExtrasProto.TransitCardExtras
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository.Valuable
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository.Valuable.RefreshPeriod
import org.koin.core.component.KoinComponent
import java.time.ZonedDateTime

abstract class BaseExtraProvider<T: Valuable, O: MessageLite>: KoinComponent {

    companion object {
        private val extraProviders: Set<BaseExtraProvider<out Valuable, out MessageLite>> = setOf(
            TrainlineProvider
        )

        suspend fun getExtrasForValuable(
            valuable: Valuable,
            currentValuable: Valuable?
        ): MessageLite? {
            return extraProviders.firstOrNull {
                it.matches(valuable)
            }?.getExtrasForValuable(valuable, currentValuable)
        }

        fun getRefreshPeriod(valuable: Valuable, time: ZonedDateTime): RefreshPeriod? {
            return extraProviders.firstOrNull {
                it.matches(valuable)
            }?.getRefreshPeriodForValuable(valuable, time)
        }

        fun getEndTimeForValuable(valuable: Valuable): ZonedDateTime? {
            return extraProviders.firstOrNull {
                it.matches(valuable)
            }?.getEndTimeForValuable(valuable)
        }

        private fun BaseExtraProvider<*, *>.matches(valuable: Valuable): Boolean {
            if (!valuableClasses.contains(valuable::class.java)) return false
            val issuerName = valuable.getIssuerInfo()?.issuerName
            if (issuerNames.contains(issuerName)) return true
            if (doesMatch(valuable)) return true
            return false
        }

    }

    /**
     *  The issuer name(s) to match for this provider, or empty if falling back to a custom method.
     */
    open val issuerNames: Set<String> = emptySet()

    /**
     *  Matcher for checking if this provider can be used for a valuable. Not used if [issuerNames]
     *  matches.
     */
    open val doesMatch: (Valuable) -> Boolean = { false }

    /**
     *  The class(es) of the Valuables to match
     */
    abstract val valuableClasses: Set<Class<T>>

    private suspend fun getExtrasForValuable(valuable: Valuable, currentValuable: Valuable?): O? {
        return getExtras(valuable as T, getExtrasFromCurrentValuable(currentValuable as? T))
    }

    private fun getRefreshPeriodForValuable(
        valuable: Valuable, time: ZonedDateTime
    ): RefreshPeriod? {
        return getRefreshPeriod(valuable as T, time)
    }

    private fun getEndTimeForValuable(valuable: Valuable): ZonedDateTime? {
        return getEndTime(valuable as T)
    }

    /**
     *  Get the extras for a currently loaded Valuable from local storage
     */
    abstract fun getExtrasFromCurrentValuable(valuable: T?): O?

    /**
     *  Get the refresh period for a given valuable, or null to use the default
     */
    open fun getRefreshPeriod(valuable: T, time: ZonedDateTime): RefreshPeriod? = null

    /**
     *  Get the current end time for a given valuable, from the local storage
     */
    open fun getEndTime(valuable: T): ZonedDateTime? = null

    /**
     *  Load extras for a given valuable, with the [currentExtras] set to the current data from
     *  local storage.
     */
    abstract suspend fun getExtras(valuable: T, currentExtras: O?): O?

}

abstract class BaseTransitCardProvider: BaseExtraProvider<Valuable.TransitCard, TransitCardExtras>() {

    override val valuableClasses = setOf(Valuable.TransitCard::class.java)

    override fun getExtrasFromCurrentValuable(valuable: Valuable.TransitCard?): TransitCardExtras? {
        return valuable?.extras
    }

}