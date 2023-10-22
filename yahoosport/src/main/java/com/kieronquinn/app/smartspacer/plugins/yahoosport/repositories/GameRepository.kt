package com.kieronquinn.app.smartspacer.plugins.yahoosport.repositories

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.gson.BitmapTypeAdapter
import com.kieronquinn.app.smartspacer.plugins.yahoosport.repositories.GameRepository.Game
import com.kieronquinn.app.smartspacer.plugins.yahoosport.targets.YahooSportTarget
import com.kieronquinn.app.smartspacer.plugins.yahoosport.targets.YahooSportTarget.TargetData
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File

interface GameRepository {

    fun getGame(id: String): Game?
    fun setGame(id: String, game: Game?)
    fun setTeamName(id: String, teamName: String?)

    data class Game(
        @SerializedName("team_1_name")
        val team1Name: String,
        @SerializedName("team_2_name")
        val team2Name: String,
        @SerializedName("team_1_icon")
        val team1Icon: Bitmap,
        @SerializedName("team_2_icon")
        val team2Icon: Bitmap,
        @SerializedName("team_1_score")
        val team1Score: String,
        @SerializedName("team_2_score")
        val team2Score: String,
        @SerializedName("date")
        val date: String,
        @SerializedName("period")
        val period: String
    ) {

        fun getGameHash(): Int {
            var result = team1Name.hashCode()
            result = 31 * result + team2Name.hashCode()
            return result
        }

    }

}

class GameRepositoryImpl(
    private val context: Context,
    private val dataRepository: DataRepository
): GameRepository {

    private val gson = GsonBuilder()
        .registerTypeAdapter(Bitmap::class.java, BitmapTypeAdapter())
        .create()

    private val scope = MainScope()

    private val gameDir = File(context.filesDir, "games").apply {
        mkdirs()
    }

    override fun getGame(id: String): Game? {
        val game = File(gameDir, "$id.json")
        if(!game.exists()) return null
        return try {
            gson.fromJson(game.readText(), Game::class.java)
        }catch (e: Exception) {
            null
        }
    }

    override fun setGame(id: String, game: Game?) {
        scope.launch {
            val file = File(gameDir, "$id.json")
            if(game == null){
                if(file.exists()) file.delete()
            }else {
                file.writeText(gson.toJson(game))
            }
            SmartspacerTargetProvider.notifyChange(context, YahooSportTarget::class.java, id)
        }
    }

    override fun setTeamName(id: String, teamName: String?) {
        dataRepository.updateTargetData(
            id,
            TargetData::class.java,
            TargetData.TYPE,
            ::onChanged
        ) {
            val data = it ?: TargetData()
            data.copy(teamName = teamName)
        }
    }

    private fun onChanged(context: Context, smartspacerId: String) {
        SmartspacerTargetProvider.notifyChange(context, YahooSportTarget::class.java, smartspacerId)
    }

}