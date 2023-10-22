package com.kieronquinn.app.smartspacer.plugin.googlewallet.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kieronquinn.app.smartspacer.plugin.shared.utils.room.EncryptedValue

@Entity
data class WalletValuable(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo("hash")
    val hash: Long,
    @ColumnInfo("proto")
    val valuable: EncryptedValue,
    @ColumnInfo("extras")
    val extras: EncryptedValue?,
    @ColumnInfo("image")
    val image: EncryptedValue?,
    @ColumnInfo("card_image")
    val cardImage: EncryptedValue?,
    @ColumnInfo("is_dismissed")
    val isDismissed: Boolean = false
)