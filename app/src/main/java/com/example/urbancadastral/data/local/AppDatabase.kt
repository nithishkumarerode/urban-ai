package com.example.urbancadastral.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.urbancadastral.data.local.dao.CadastralDao
import com.example.urbancadastral.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CadastralProjectEntity::class,
        DroneDatasetEntity::class,
        ProcessingJobEntity::class,
        ParcelEntity::class,
        BuildingFootprintEntity::class,
        BoundaryConflictEntity::class,
        GroundTruthTaskEntity::class,
        SurveyEvidenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cadastralDao(): CadastralDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "urban_cadastral_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialCadastralData(database.cadastralDao())
                    }
                }
            }
        }

        private suspend fun populateInitialCadastralData(dao: CadastralDao) {
            // Strict ground-truth cadastral policy:
            // No fake coordinates or synthetic parcel polygons are seeded.
            // Displays 'NO GIS DATA LOADED' until actual datasets are uploaded or drawn.
        }
    }
}
