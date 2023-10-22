package com.kieronquinn.app.smartspacer.plugin.googlekeep.repositories

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.googlekeep.model.Note
import com.kieronquinn.app.smartspacer.plugin.googlekeep.targets.GoogleKeepTarget
import com.kieronquinn.app.smartspacer.plugin.googlekeep.targets.GoogleKeepTarget.TargetData
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.MainScope

interface KeepRepository {

    fun setNote(smartspacerId: String, note: Note)
    fun getTargetData(smartspacerId: String): TargetData?
    fun deleteTargetData(smartspacerId: String)

}

class KeepRepositoryImpl(
    private val dataRepository: DataRepository
): KeepRepository {

    private val scope = MainScope()

    override fun setNote(smartspacerId: String, note: Note) {
        updateTargetData(smartspacerId, note)
    }

    override fun getTargetData(smartspacerId: String): TargetData? {
        return dataRepository.getTargetData(smartspacerId, TargetData::class.java)
    }

    override fun deleteTargetData(smartspacerId: String) {
        dataRepository.deleteTargetData(smartspacerId)
    }

    private fun updateTargetData(smartspacerId: String, note: Note) {
        dataRepository.updateTargetData(
            smartspacerId,
            TargetData::class.java,
            TargetData.TYPE,
            ::onTargetUpdated
        ) {
            val current = it ?: TargetData()
            current.copy(note = note)
        }
    }

    private fun onTargetUpdated(context: Context, smartspacerId: String) {
        SmartspacerTargetProvider.notifyChange(context, GoogleKeepTarget::class.java, smartspacerId)
    }

}