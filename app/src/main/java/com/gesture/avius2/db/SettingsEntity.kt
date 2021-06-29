package com.gesture.avius2.db

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.gesture.avius2.model.Question
import com.gesture.avius2.model.QuestionMultiLang

@Entity(tableName = "settings")
data class SettingsEntity(
    var themeColor: String,
    var logo: String,
    // We want to replace the same id over and over so we don't get two versions
    // as the use case is settings so we need KEY_VALUE pair storage
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)