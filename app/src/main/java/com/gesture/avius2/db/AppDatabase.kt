package com.gesture.avius2.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [QuestionEntity::class, SettingsEntity::class],
    version = 1, exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun questionsDao(): QuestionsDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * @param scope will be used only to delete the older data if configured
         */
        fun getInstance(context: Context, scope: CoroutineScope? = null): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context, scope).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context, scope: CoroutineScope?): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "avius.touchless.ui.database"
            )
//                .addCallback(DeleteCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    private class DeleteCallback(private val scope: CoroutineScope?) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope?.launch {
                    deleteAll(database.questionsDao())
                }
            }
        }

        suspend fun deleteAll(questionsDao: QuestionsDao) {
            questionsDao.deleteAll()
        }

    }
}